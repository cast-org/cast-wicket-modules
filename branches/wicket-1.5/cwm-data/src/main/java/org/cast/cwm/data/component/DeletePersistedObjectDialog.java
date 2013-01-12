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
import lombok.Setter;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
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
		
		
		
		add(dialogBorder = new DialogBorder ("dialogBorder", new AbstractReadOnlyModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return "Delete " + Strings.capitalize(objectName);
			}
			
		}));
	
		dialogBorder.setMoveContainer(this);
		
		dialogBorder.getBodyContainer().add(new Label("objectType", new PropertyModel<String>(this, "objectName")));
		
		dialogBorder.getBodyContainer().add(new WebMarkupContainer("cancelLink").add(dialogBorder.getClickToCloseBehavior()));
		
		dialogBorder.getBodyContainer().add(getDeleteLink("deleteLink", model));
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
	
	public IBehavior getClickToOpenBehavior() {
		return getDialogBorder().getClickToOpenBehavior();
	}
	
	public IBehavior getClickToCloseBehavior() {
		return getDialogBorder().getClickToCloseBehavior();
	}

	protected abstract void deleteObject();
	
}
