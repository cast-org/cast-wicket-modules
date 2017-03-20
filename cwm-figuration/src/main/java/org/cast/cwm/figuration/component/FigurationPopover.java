/*
 * Copyright 2011-2016 CAST, Inc.
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
package org.cast.cwm.figuration.component;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.model.IModel;

/**
 * Base class for popovers built with Figuration.
 * The content of the Popover is a Component, which must use the ID given by {@link #getContentId()}.
 * The popover can be opened by client side Javascript, by a component with a
 * {@link org.cast.cwm.figuration.behavior.PopoverTriggerBehavior} attached, or as part of an AJAX request using
 * {@link #show(Component, org.apache.wicket.ajax.AjaxRequestTarget)} .
 * 
 * Designed to work similarly to org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.
 * 
 * @author bgoldowsky
 */
public class FigurationPopover<T> extends FigurationHideable<T> {

	public static final String CONTENT_ID = "content";

	public FigurationPopover(String id) {
		this(id, null);
		// set a default of empty content.
		add(new EmptyPanel(CONTENT_ID).setOutputMarkupPlaceholderTag(true));
	}

	public FigurationPopover(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	protected CharSequence getShowMethodCall(String triggerMarkupId) {
		// For popovers, need to call 'show' method after initialization
		return super.getShowMethodCall(triggerMarkupId)
				+ String.format("$('#%s').%s('show');",
					triggerMarkupId,
					getInitializationFunctionName());
	}

	/**
	 * Returns the ID to use for the content component of the popover.
	 */
	public static String getContentId() {
		return CONTENT_ID;
	}

	/**
	 * Sets the content of the popover to the given component.
	 *
	 * @param component content component
	 * @return this for chaining
	 */
	public FigurationPopover<T> setContent(final Component component) {
		if (component.getId().equals(getContentId()) == false) {
			throw new WicketRuntimeException("Popover content id is wrong. Component ID:" +
					component.getId() + "; required ID: " + getContentId());
		} else if (component instanceof AbstractRepeater) {
			// Not sure if this restriction is necessary, since we repaint the whole object on open/close.
			throw new WicketRuntimeException(
					"A repeater component cannot be used as the content of a modal window, please use repeater's parent");
		}
		addOrReplace(component);
		return this;
	}
	@Override
	public String getInitializationFunctionName() {
		return "CFW_Popover";
	}

}
