/*
 * Copyright 2011-2014 CAST, Inc.
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
import lombok.Setter;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.data.PersistedObject;

/**
 * A dialog for deleting a {@link Persisted Object} from the datastore.
 * 
 * TODO: Is this too specific for a separate panel?  Shouldn't we break these down into a few types of Dialogs:
 *  - Alert Dialog (takes a message and shows Okay)
 *  - Confirm Dialog (takes a message and shows Okay, Cancel)
 *  - Prompt Dialog (takes a message and IModel<String> and shows TextField, Okay, Cancel)
 *  
 *  All of them should be AJAX. 
 * 
 * @author jbrookover
 *
 * @param <T>
 */
public abstract class DeletePersistedObjectDialog<T extends PersistedObject> extends Panel {

	@Getter
	protected DialogBorder dialogBorder;
	
	/**
	 * Method for determining the name of the object being deleted - used in the title and the message.
	 */
	@Getter @Setter
	protected String objectName;
	
	private static final long serialVersionUID = 1L;

	public DeletePersistedObjectDialog(String id, IModel<T> model) {
		super(id, model);

		// Set up models that allow property values to interpolate the type of object being deleted with {0},
		// and its capitalized form with {1}.
		Object[] modelParameters = new Object[] {
				new PropertyModel<String>(DeletePersistedObjectDialog.this, "objectName"),
				new PropertyModel<String>(DeletePersistedObjectDialog.this, "objectNameCapitalized")
		};
		
		StringResourceModel titleModel = new StringResourceModel("deleteDialogTitle", this, null, modelParameters);
		add(dialogBorder = new DialogBorder ("dialogBorder", titleModel));
		dialogBorder.setMoveContainer(this);
		
		// Set up a model that allows property value to interpolate the type of object being deleted with {0}
		StringResourceModel messageModel = new StringResourceModel("deleteQuestion", this, null, modelParameters);
		dialogBorder.getBodyContainer().add(new Label("deleteQuestion", messageModel));
		
		dialogBorder.getBodyContainer().add(new WebMarkupContainer("cancelLink").add(dialogBorder.getClickToCloseBehavior()));
		
		dialogBorder.getBodyContainer().add(getDeleteLink("deleteLink", model));
	}
	
	public String getObjectNameCapitalized() {
		return Strings.capitalize(objectName);
	}
	
	@SuppressWarnings("unchecked")
	public IModel<T> getModel() {
		return (IModel<T>) getDefaultModel();
	}
	
	public T getModelObject() {
		return getModel().getObject();
	}
	
	/**
	 * Returns the link that will be used to delete this object.
	 * 
	 * @param id
	 * @return
	 */
	protected Link<T> getDeleteLink(String id, IModel<T> model) {
		return new Link<T>(id, model) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				deleteObject();
			}
		};
	}
	
	public Behavior getClickToOpenBehavior() {
		return getDialogBorder().getClickToOpenBehavior();
	}
	
	public Behavior getClickToCloseBehavior() {
		return getDialogBorder().getClickToCloseBehavior();
	}

	protected abstract void deleteObject();
	
}
