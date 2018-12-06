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
package org.cast.cwm.figuration.hideable;

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

/**
 * Add this behavior to a button or other component that should cause a FigurationHideable to open/close.
 *
 * After creating a component with one of the hideable types (FigurationModal, FigurationPopover,
 * FigurationCollapse, or FigurationTooltip), construct this behavior and attach it to the button
 * that should open that object.  This will add any linking attributes that are needed, and
 * call the Javascript function to initialize the behavior.
 *
 * This can also be used in cases where there are many buttons for one dynamically-constructed popover or
 * modal.  See {@link #initializeOnLoad} and {@link FigurationHideable#connectAndShow(Component, AjaxRequestTarget)}.
 * 
 * @author bgoldowsky
 *
 */
public class FigurationTriggerBehavior extends Behavior {

	@Getter
	protected final FigurationHideable<?> target;

	/**
	 * Whether this trigger should be connected and initialized on load.
	 * Normally triggers and their hideable elements are one-to-one, and should be
	 * connected and initialized immediately.
	 * However, if you are re-using a single hideable element to be triggered dynamically by
	 * several possible triggers, you can set this to false and later use connectAndShow
	 * to initialize just in time.
	 */
	@Getter
	@Setter
	protected boolean initializeOnLoad = true;


	public FigurationTriggerBehavior(FigurationHideable target) {
		this.target = target;
	}
	
	@Override
	public void onConfigure(Component component) {
		super.onConfigure(component);
		// Trigger components must have a markupId available to Javascript.
		component.setOutputMarkupId(true);
		if (initializeOnLoad)
			target.setTriggerComponent(component);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		if (initializeOnLoad && component.isEnabledInHierarchy())
			response.render(OnLoadHeaderItem.forScript(target.getInitializeJavascript()));
	}

}