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
package org.cast.cwm.components;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * A TextArea that will dynamically resize itself to a height that fits its current content.
 * NOTE: your textarea MUST use the cols and rows attributes to set a default size,
 * otherwise it will default to some browser-defined default size.  CSS sizing of the text area will 
 * not work when this Javascript is in effect.
 * 
 * There are many Javascript libraries that try to do autosizing, but none that I've tested
 * work well in all situations.
 * 
 * Tried:
 *   http://www.technoreply.com/autogrow-textarea-plugin-version-2-0
 *      Works reasonably well cross browser, but depends on (deprecated) rows and cols attributes.
 *   Chris Bader's autogrow,  https://github.com/akaihola/jquery-autogrow .
 *      Fails completely on Chrome, heights get slightly off eventually.
 *   Brinley Ang's patch to the above, https://github.com/brinley/jquery-autogrow
 *      Better on Chrome, but still gets off with enough random content.
 *   http://github.com/jaz303/jquery-grab-bag/tree/master/javascripts/jquery.autogrow-textarea.js
 *   http://www.jacklmoore.com/autosize
 *   http://unwrongest.com/projects/elastic/
 *      Fails in FF 15
 * 
 *  The current code is based on the technoreply.com script.
 *      
 * @author bgoldowsky
 *
 * @param <T>  the model type of the TextArea
 */
public class AutoGrowTextArea<T> extends TextArea<T> {

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
		// This script works well but requires the text area to have cols and rows attributes - CSS sizing doesn't work
		 response.renderJavaScriptReference(new PackageResourceReference(AutoGrowTextArea.class, "jquery.autogrow.techno.js"));
		 // Must be onLoad, not onDomReady, or else it happens too early in AJAX insertion
		 response.renderOnLoadJavaScript(String.format("$('#%s').autoGrow();", getMarkupId()));
	}


}
