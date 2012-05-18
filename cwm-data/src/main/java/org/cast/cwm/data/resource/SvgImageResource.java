/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.cwm.data.resource;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.Application;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.ResponseData;
import org.cast.cwm.drawtool.SvgEditor;
import org.cast.cwm.service.CwmService;
import org.cast.cwm.service.ICwmService;

import com.google.inject.Inject;

/**
 * A simple resource that accepts an "id" parameter and serves up
 * the ResponseData svg file with the matching ID in the database.  
 * If the file is not found, this will throw a 404 Not Found Error.
 * 
 * @author jbrookover
 *
 */
@Slf4j
public class SvgImageResource extends DynamicWebResource {
	
	public static final String SVG_PATH = "svg";
	private static boolean mounted = false;
	
	private static final long serialVersionUID = 1L;

	@Inject
	private ICwmService cwmService;

	public SvgImageResource() {
		super();
		InjectorHolder.getInjector().inject(this);
		// Cannot be cacheable, otherwise WicketFilter will cause database access when it checks the last-modified
		// date, before the session context has been set up, and this database session can remain unclosed.
		//setCacheable(true);
	}
	
	@Override
	protected ResourceState getResourceState() {
		// Check ID parameter; throw 404 if invalid
		Long id = getParameters().getAsLong("id");		
		if (id == null) {
			log.warn("Invalid SVG request: null id");
			throw new AbortWithWebErrorCodeException(HttpServletResponse.SC_NOT_FOUND, "Invalid Svg Id");	
		}
		ResponseData rd = cwmService.getById(ResponseData.class, id).getObject();
		if (rd == null) {
			log.warn("Invalid SVG request, id {} does not exist", id);
			throw new AbortWithWebErrorCodeException(HttpServletResponse.SC_NOT_FOUND, "Svg not found [id=" + id + "]");
		}
		
		Integer width = getParameters().getAsInteger("width", SvgEditor.CANVAS_WIDTH);
		Integer height = getParameters().getAsInteger("height", SvgEditor.CANVAS_HEIGHT);
		boolean needsScaling = (width < SvgEditor.CANVAS_WIDTH || height < SvgEditor.CANVAS_HEIGHT);

		log.debug("Getting SVG data for {} at width {}", id, width);
		String svg = rd.getText();
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version='1.0' encoding='UTF-8' ?>");
		// TODO: Should this be "StartsWith" ?
		if (svg == null || !svg.contains("<svg")) {
			buffer.append("<svg width='" + width + "' height='" + height + "' xmlns:xlink='http://www.w3.org/1999/xlink' xmlns='http://www.w3.org/2000/svg'><g><title>Blank Image</title></g></svg>");
		} else {
			if (needsScaling) {
				svg = svg.replaceFirst("<svg +width=\"[0-9]*\" +height=\"[0-9]*\"", 
						"<svg viewBox='0 0 " + SvgEditor.CANVAS_WIDTH + " " + SvgEditor.CANVAS_HEIGHT + "' "
						+ "width='" + width + "' height='" + height + "' "
						+ "xml:base=\"" + WebApplication.get().getServletContext().getContextPath() + "/\" ");
			} else {
				svg = svg.replaceFirst("<svg ", "<svg xml:base=\"" + WebApplication.get().getServletContext().getContextPath() + "/\" ");
			}
			buffer.append(svg);
		}
		
		return new SvgResourceState(Time.valueOf(rd.getCreateDate()), buffer.toString().getBytes());
	}

	public static void mount(WebApplication app) {
		app.getSharedResources().add(SVG_PATH, new SvgImageResource());
		app.mountSharedResource("/" + SVG_PATH, Application.class.getName() + "/" + SVG_PATH);
		mounted = true;
	}
	
	public static String constructUrl(ResponseData rd, Integer width, Integer height) {
		if (!mounted)
			throw new IllegalStateException("SvgImageResource has not been mounted in the application.");
		StringBuffer url = new StringBuffer(WebApplication.get().getServletContext().getContextPath() + "/" + SVG_PATH + "/id/" + rd.getId());
		url.append("?width=" + width + "&height=" + height);
		return url.toString();
	}
	
	
	protected class SvgResourceState extends ResourceState {
		private Time createDate;
		private byte[] svgData;

		protected SvgResourceState (Time createDate, byte[] svgData) {
			this.createDate = createDate;
			this.svgData = svgData;
		}

		@Override
		public String getContentType() { 
			return "image/svg+xml";
		}

		@Override
		public byte[] getData() { 
			return svgData;
		}
		
		@Override
		public Time lastModifiedTime() { 
			return createDate;
		}

	}

}
