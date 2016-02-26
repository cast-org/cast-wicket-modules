/*
 * Copyright 2011-2016 CAST, Inc.
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
package org.cast.cwm.data.component;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.resource.SvgImageResourceReference;
import org.cast.cwm.drawtool.SvgEditor;

/**
 * Display an SVG Image.  This must be attached to an iFrame since most browsers don't 
 * like to mix SVG and HTML.
 * 
 * @author jbrookover
 *
 */
public class SvgImage extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;	
	
	private CharSequence imageUrl;
	private int width = SvgEditor.CANVAS_WIDTH;
	private int height = SvgEditor.CANVAS_HEIGHT;
	private Integer maxWidth;
	private Integer maxHeight;
		
	public SvgImage(String id, IModel<? extends Response> model, Integer maxWidth, Integer maxHeight) {
		super(id, model);
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (maxWidth != null && width > maxWidth) {
			float ratio = (float)maxWidth/(float)width;
			height = Math.round(height * ratio);
			width = Math.round(width * ratio);
		}
		if (maxHeight != null && height > maxHeight) {
			float ratio = (float)maxHeight/(float)height;
			height = Math.round(height * ratio);
			width = Math.round(width * ratio);
		}
		
		Response response = (Response)getDefaultModel().getObject();
		if (response.getText() != null) {
			PageParameters pp = new PageParameters()
				.set("id", response.getResponseData().getId())
				.set("width", width)
				.set("height", height);
			imageUrl = getRequestCycle().urlFor(new SvgImageResourceReference(), pp);
		}
	}
	
	@Override
	protected final void onComponentTag(final ComponentTag tag) {
		checkComponentTag(tag, "iframe");
		if (imageUrl != null)
			tag.put("src", imageUrl);
		tag.put("style", "background:#FFFFFF");
		tag.put("width", width);
		tag.put("height", height);
		tag.put("title", "Image Container");
		super.onComponentTag(tag);
	}
	
}
