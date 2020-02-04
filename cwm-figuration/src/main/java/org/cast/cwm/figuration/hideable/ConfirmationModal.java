/*
 * Copyright 2011-2020 CAST, Inc.
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
package org.cast.cwm.figuration.hideable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * A modal dialog with "confirm" (ok, yes, etc) button and a "cancel" button.
 * 
 * Give this dialog a purpose by overriding the onConfirm method.
 * Set the title, message, and button texts through properties or setter methods.
 *
 * @param <T> model object type. A model is not required, so this can be Void.
 */
public abstract class ConfirmationModal<T> extends FigurationModal<T> {

	public ConfirmationModal(String id) {
		this(id, null);
	}

	public ConfirmationModal(String id, IModel<T> model) {
		super(id, model);

		add(new FigurationModalBasicHeader("header",
				new StringResourceModel("modalTitle", this)));

		add(new Label("message",
				new StringResourceModel("message", this)));

		add(new Label("cancel",
				new StringResourceModel("cancelButtonLabel", this)));

		add(new AjaxLink<Void>("confirm") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				this.setBody(new StringResourceModel("confirmButtonLabel", this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if(onConfirm(target))
					ConfirmationModal.this.hide(target);
			}
		});

	}

	public ConfirmationModal<T> setHeaderTextModel(IModel<String> model) {
		get("header").setDefaultModel(model);
		return this;
	}

	public ConfirmationModal<T> setMessageTextModel(IModel<String> model) {
		get("message").setDefaultModel(model);
		return this;
	}

	public ConfirmationModal<T> setConfirmButtonTextModel(IModel<String> model) {
		((AjaxLink)get("confirm")).setDefaultModel(model);
		return this;
	}

	public ConfirmationModal<T> setCancelButtonTextModel(IModel<String> model) {
		get("cancel").setDefaultModel(model);
		return this;
	}

	public ConfirmationModal<T> setHeaderText(String text) {
		return setHeaderTextModel(Model.of(text));
	}

	public ConfirmationModal<T> setMessageText(String text) {
		return setMessageTextModel(Model.of(text));
	}

	public ConfirmationModal<T> setConfirmButtonText(String text) {
		return setConfirmButtonTextModel(Model.of(text));
	}

	public ConfirmationModal<T> setCancelButtonText(String text) {
		return setCancelButtonTextModel(Model.of(text));
	}

	/**
	 * Called when "Confirm" button in the modal is clicked.
	 * Normally, the modal will close at this point, but you can avoid that by returning "false" from this method.
	 *
	 * @param target AJAX request target
	 * @return true if modal should be closed
	 */
	protected abstract boolean onConfirm(AjaxRequestTarget target);
	
}
