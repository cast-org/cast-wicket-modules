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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * TODO: This is too specific.  See DeletePersistedObjectDialog for comments
 * 
 * @author jbrookover
 *
 */
public abstract class AjaxCancelChangesDialog extends Panel {

	@Getter
	protected DialogBorder dialogBorder;
	
	private static final long serialVersionUID = 1L;

	public AjaxCancelChangesDialog(String id) {
		super(id);
		
		add(dialogBorder = new DialogBorder ("dialogBorder", "Cancel Changes"));
		
		dialogBorder.getBodyContainer().add(new WebMarkupContainer("noCancelLink").add(dialogBorder.getClickToCloseBehavior()));
		
		dialogBorder.getBodyContainer().add(new AjaxFallbackLink<Void>("cancelLink") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
				dialogBorder.close(target);
			}
		});
	}

	protected abstract void onCancel(AjaxRequestTarget target);
	
	public IBehavior getClickToCloseBehavior() {
		return dialogBorder.getClickToCloseBehavior();
	}

	public IBehavior getClickToOpenBehavior() {
		return dialogBorder.getClickToOpenBehavior();
	}

}
