/*
 * Copyright 2011-2019 CAST, Inc.
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
package org.cast.cwm.components.accessibility;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * TODO: Use this someday when we can not add two new GET requests to each page load.
 * 
 * For now, DO NOT USE.
 * 
 * @author jbrookover
 *
 */
public class SkipLink extends Panel {

	private static final long serialVersionUID = 1L;

	public SkipLink(String id) {
		super(id);
		add(new Label("skipLinkText", new StringResourceModel("skipLinkText", this, null)));
	}

	public void renderHead(IHeaderResponse response) {
		response.renderCSSReference(new PackageResourceReference(SkipLink.class, "skiplink.css"));
		response.renderJavaScriptReference(new JavaScriptResourceReference(SkipLink.class, "skiplink.js"));
		
	}
}
