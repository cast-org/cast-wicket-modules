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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.PersistedObject;

/**
 * A dialog for deleting a {@link Persisted Object} from the datastore.
 * 
 * @author jbrookover
 *
 * @param <T>
 */
public abstract class AjaxDeletePersistedObjectDialog<T extends PersistedObject> extends DeletePersistedObjectDialog<T> {

	private static final long serialVersionUID = 1L;

	public AjaxDeletePersistedObjectDialog(String id, IModel<T> model) {
		super(id, model);
	}
	
	/**
	 * Returns the link that will be used to delete this object.
	 * 
	 * @param id
	 * @return
	 */
	@Override
	protected Link<T> getDeleteLink(String id, IModel<T> model) {
		return new AjaxFallbackLink<T>(id) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				deleteObject(target);
				dialogBorder.close(target); // Close Dialog
			}
		};
	}
	
	
	@Override
	final protected void deleteObject() {
		deleteObject(null);
	}
	
	protected abstract void deleteObject(AjaxRequestTarget target);
	
}
