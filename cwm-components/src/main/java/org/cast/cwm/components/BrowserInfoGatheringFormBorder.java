/*
 * Copyright 2011-2017 CAST, Inc.
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

import org.apache.wicket.markup.html.border.Border;

/**
 * A Border that adds extended-browser-information gathering capability to a (stateless) Form.
 * Additional form fields that you actually want can be added inside the Border.
 * Use {#getForm()} to get access to the actual Form object inside the Border,
 * and override {#onSubmit()} to add your own submit behavior.
 *  
 * @author bgoldowsky
 *
 * @param <T> model type for the Form
 */
public class BrowserInfoGatheringFormBorder<T> extends Border {

	private static final long serialVersionUID = 1L;

	BrowserInfoGatheringForm<T> form;
	
	public BrowserInfoGatheringFormBorder(String id) {
		super(id);
		form = new BrowserInfoGatheringForm<T>("postback") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit()	{
				super.onSubmit();
				BrowserInfoGatheringFormBorder.this.onSubmit();
			}
		};
		addToBorder(form);
		// The border's body is inside the form.
		form.add(getBodyContainer());
	}
	
	/**
	 * Called when the enclosed Form is submitted, after processing the browser info.
	 * Override this, and call super.onSubmit(), to add submit behavior to the Border's Form.
	 */
	protected void onSubmit() {
	}

	protected BrowserInfoGatheringForm<T> getForm() {
		return form;
	}
	
}
