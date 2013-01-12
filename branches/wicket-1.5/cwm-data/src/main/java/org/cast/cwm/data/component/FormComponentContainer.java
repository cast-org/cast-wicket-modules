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
package org.cast.cwm.data.component;

import lombok.Getter;

import org.apache.wicket.Session;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.model.PropertyModel;

/**
 * This class wraps a {@link FormComponent} in a feedback border, adds a label for the 
 * component, and acts as an enclosure if the form component is not visible.
 * 
 * @author jbrookover
 *
 */
public class FormComponentContainer extends Border {
	
	private static final long serialVersionUID = 1L;
	
	@Getter
	private String label = "Label";
	
	@Getter
	private FormComponent<?> formComponent;
	
	private boolean hasErrors = false;

	/**
	 * Constructor.  The component is added here, so you do not add it
	 * in a separate statement.
	 * 
	 * @param id
	 * @param component
	 */
	public FormComponentContainer(String id, FormComponent<?> component) {
		super(id);
		
		setOutputMarkupPlaceholderTag(true);
		
		this.formComponent = component;
		
		// TODO: Create custom feedback border that shows a filter based on FormComponentContainer
		// TODO: OR see if you can add the component directly to the feedbackBorder
		add(new FormComponentLabel("label", formComponent).add(new Label("labelText", new PropertyModel<String>(FormComponentContainer.this, "label"))));
		
		add(new WebMarkupContainer("errorIndicator") {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return hasErrors;
			}
		});
		
		add(component);
	}
	
	/**
	 * Set the <label> for this form component.
	 * @param label
	 * @return
	 */
	public FormComponentContainer setLabel(String label) {
		this.label = label;
		return this;  // For Chaining
	}
	
	/**
	 * Convenience method for setting the wrapped {@link FormComponent}
	 * to be required.
	 * 
	 * @param required
	 * @return
	 */
	public FormComponentContainer setRequired(boolean required) {
		this.formComponent.setRequired(required);
		return this; // For Chaining
	}
	
	
	@Override
	public boolean isVisible() {
		return super.isVisible() && formComponent.isVisible();
	}
	
	@Override
	protected void onBeforeRender() {
		hasErrors = Session.get().getFeedbackMessages().messages(new ContainerFeedbackMessageFilter(this)).size() != 0;
		super.onBeforeRender();
	}

}
