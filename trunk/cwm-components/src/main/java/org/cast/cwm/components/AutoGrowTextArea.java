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
package org.cast.cwm.components;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

/**
 * A TextArea that will automatically resize itself.
 * Uses Chris Bader's Javascript library, pulled from https://github.com/akaihola/jquery-autogrow .
 * 
 * @author bgoldowsky
 *
 * @param <T>  the model type of the TextArea
 */
public class AutoGrowTextArea<T> extends TextArea<T> implements IHeaderContributor {

	private static final long serialVersionUID = 1L;

	public AutoGrowTextArea (String id) {
		super(id);
		this.setOutputMarkupId(true);
	}
	
	public AutoGrowTextArea (String id, IModel<T> model) {
		super(id, model);
		this.setOutputMarkupId(true);
	}

	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(new ResourceReference(AutoGrowTextArea.class, "jquery.autogrow.js"));
		response.renderOnDomReadyJavascript(String.format("$('#%s').autogrow();", getMarkupId()));
	}


}
