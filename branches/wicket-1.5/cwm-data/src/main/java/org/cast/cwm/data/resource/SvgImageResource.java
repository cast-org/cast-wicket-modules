/*
 * Copyright 2011-2013 CAST, Inc.
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

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.ResponseData;
import org.cast.cwm.data.UserContent;
import org.cast.cwm.drawtool.SvgEditor;
import org.cast.cwm.service.ICwmService;

import com.google.inject.Inject;

/**
 * A simple resource that accepts an "id" parameter and serves up
 * the ResponseData svg file with the matching ID in the database.  
 * If the file is not found, this will throw a 404 Not Found Error.
 * 
 * For use with UserContent rather than Response, send a "uid" parameter 
 * instead of "id".
 * 
 * @author jbrookover
 *
 */
@Slf4j
public class SvgImageResource extends DynamicImageResource {
	
	private static final long serialVersionUID = 1L;

	@Inject
	private ICwmService cwmService;

	public SvgImageResource() {
		super();
		Injector.get().inject(this);
		setFormat("svg+xml");

		// TODO: see how this old comment translates into Wicket 1.5:
		// Cannot be cacheable, otherwise WicketFilter will cause database access when it checks the last-modified
		// date, before the session context has been set up, and this database session can remain unclosed.
		//setCacheable(true);
	}
	
	@Override
    protected byte[] getImageData(Attributes attributes) {
        PageParameters parameters = attributes.getParameters();
        Long id = parameters.get("id").toOptionalLong();
        Long uid = parameters.get("uid").toOptionalLong();
        Integer width = parameters.get("width").toOptionalInteger();
        Integer height = parameters.get("height").toOptionalInteger();
        
        String svg;
        if (id != null) {
        	log.debug("Getting SVG Response data for id={} at width {}", id, width);
        	ResponseData rd = cwmService.getById(ResponseData.class, id).getObject();
        	if (rd == null) {
        		log.warn("Invalid SVG request, id {} does not exist", id);
        		throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND, "Svg not found [id=" + id + "]");
        	}
        	svg = rd.getText();
    		setLastModifiedTime(Time.valueOf(rd.getCreateDate()));
        } else if (uid != null) {
        	log.debug("Getting SVG UserContent data for id={} at width {}", uid, width);
        	UserContent uc = cwmService.getById(UserContent.class, uid).getObject();
        	if (uc == null) {
        		log.warn("Invalid SVG request, uid {} does not exist", id);
        		throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND, "Svg not found [uid=" + uid + "]");
        	}
        	svg = uc.getText();
        	setLastModifiedTime(Time.now()); // FIXME last updated is not getting updated!
//        	if (uc.getLastUpdated() != null)
//        		setLastModifiedTime(Time.valueOf(uc.getLastUpdated()));
//        	else
//        		setLastModifiedTime(Time.valueOf(uc.getCreateDate()));
        } else {
    		throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND, "Svg not found; no ID in URL");        	
        }
        
        return buildSvgData(svg, width, height);
	}
	
	protected byte[] buildSvgData(String svgBase, Integer width, Integer height) {
        if (width==null) width=SvgEditor.CANVAS_WIDTH;
        if (height==null) height=SvgEditor.CANVAS_HEIGHT;
		boolean needsScaling = (width < SvgEditor.CANVAS_WIDTH || height < SvgEditor.CANVAS_HEIGHT);

		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version='1.0' encoding='UTF-8' ?>");
		// TODO: Should this be "StartsWith" ?
		if (svgBase == null || !svgBase.contains("<svg")) {
			buffer.append("<svg width='" + width + "' height='" + height + "' xmlns:xlink='http://www.w3.org/1999/xlink' xmlns='http://www.w3.org/2000/svg'><g><title>Blank Image</title></g></svg>");
		} else {
			if (needsScaling) {
				svgBase = svgBase.replaceFirst("<svg +width=\"[0-9]*\" +height=\"[0-9]*\"", 
						"<svg viewBox='0 0 " + SvgEditor.CANVAS_WIDTH + " " + SvgEditor.CANVAS_HEIGHT + "' "
						+ "width='" + width + "' height='" + height + "' "
						+ "xml:base=\"" + WebApplication.get().getServletContext().getContextPath() + "/\" ");
			} else {
				svgBase = svgBase.replaceFirst("<svg ", "<svg xml:base=\"" + WebApplication.get().getServletContext().getContextPath() + "/\" ");
			}
			buffer.append(svgBase);
		}
		try {
			return buffer.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Character code error");
		}
	}
	
    @Override
    public boolean equals(Object that) {
        return that instanceof SvgImageResource;
    }

//	public static String constructUrl(ResponseData rd, Integer width, Integer height) {
//		if (!mounted)
//			throw new IllegalStateException("SvgImageResource has not been mounted in the application.");
//		StringBuffer url = new StringBuffer(WebApplication.get().getServletContext().getContextPath() + "/" + SVG_PATH + "/id/" + rd.getId());
//		url.append("?width=" + width + "&height=" + height);
//		return url.toString();
//	}
//	

}
