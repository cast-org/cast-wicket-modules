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
package org.cast.cwm.figuration.hideable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Base class for popovers built with Figuration.
 *
 * A simple popover can be created with the default markup as follows:
 * <code><pre>
 *     new FigurationPopover&lt;Void>("id")
 *         .withTitle("Popover title")
 *         .withBody(bodyPanel);
 * </pre></code>
 *
 * Wicket IDs of the header and body are fixed by FigurationHideable.
 * Components for each must be supplied.
 *
 * Alternatively, override this and provide your own markup to build a custom Popover.
 * You still have to make sure the markup includes divs with the correct class attributes,
 * just like the markup for this class, since that's how Figuration attaches its styling and behavior.
 *
 * Popovers can be opened and closed in various ways: by client side Javascript, by a
 * {@link FigurationTriggerBehavior}, or as part of an AJAX request using
 * {@link FigurationHideable#show(AjaxRequestTarget)} or
 * {@link FigurationHideable#connectAndShow(Component, AjaxRequestTarget)}.
 *
 * This class does not do any event logging itself, but you can easily log an event when a popover is
 * opened or closed (or any other supported event) using org.cast.cwm.data.behavior.EventLoggingBehavior, eg:
 * <code><pre>popover.add(new EventLoggingBehavior("beforeShow.cfw.popover", eventType));</pre></code>
 *
 * @author bgoldowsky
 */
public class FigurationPopover<T> extends FigurationTooltip<T> {

	public FigurationPopover(String id) {
		this(id, null);
	}

	public FigurationPopover(String id, IModel<T> model) {
		super(id, model);
	}

	/**
	 * Adds a simple header to the popover with the given title and a close button.
	 *
	 * @param title Title that will be shown in the header of the modal.
	 * @return this, for chaining
	 */
	public FigurationPopover<T> withTitle(String title) {
		return withTitle(Model.of(title));
	}

	/**
	 * Adds a simple header to the popover with the given title and a close button.
	 *
	 * @param mTitle Model of the title that will be shown in the header of the popover.
	 * @return this, for chaining
	 */
	public FigurationPopover<T> withTitle(IModel<String> mTitle) {
		add(new Label(HEADER_ID, mTitle));
		return this;
	}

	/**
	 * Adds the given Panel as the body of this popover.
	 *
	 * @param bodyPanel panel to use as the body
	 * @return this, for chaining
	 */
	public FigurationPopover<T> withBody(Panel bodyPanel) {
		super.withBody(bodyPanel);
		return this;
	}

	@Override
	public String getInitializationFunctionName() {
		return "CFW_Popover";
	}

	@Override
	public String getClassAttribute() {
		return "popover";
	}
}
