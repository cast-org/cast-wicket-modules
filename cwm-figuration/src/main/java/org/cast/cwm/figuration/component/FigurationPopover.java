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
package org.cast.cwm.figuration.component;

import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Map;

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
 * Alternatively, override this and its markup to build a custom Popover.
 * Make sure the markup includes divs with the correct class attributes.
 *
 * Popovers can be opened and closed in various ways: by client side Javascript, by a component with a
 * {@link org.cast.cwm.figuration.behavior.PopoverTriggerBehavior} attached, or as part of an AJAX request using
 * {@link FigurationHideable#show(Component, AjaxRequestTarget)}.
 *
 * This class does not do any event logging itself, but you can easily log an event when a popover is
 * opened or closed (or any other supported event) using org.cast.cwm.data.behavior.EventLoggingBehavior, eg:
 * <code><pre>popover.add(new EventLoggingBehavior("beforeShow.cfw.popover", eventType));</pre></code>
 *
 * @author bgoldowsky
 */
public class FigurationPopover<T> extends FigurationHideable<T> {

	public FigurationPopover(String id) {
		this(id, null);
	}

	public FigurationPopover(String id, IModel<T> model) {
		super(id, model);
		addClassAttributeModifier();
	}

	/**
	 * Sets up a ClassAttributeModifier that adds the figuration-required class attribute to the top level tag.
	 */
	protected void addClassAttributeModifier() {
		add(ClassAttributeModifier.append("class", "popover"));
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
		if (!bodyPanel.getId().equals(BODY_ID))
			throw new IllegalArgumentException("Body panel must have id " + BODY_ID);
		addOrReplace(bodyPanel);
		return this;
	}

	@Override
	protected Map<String, String> getShowParameters() {
		Map<String, String> map = super.getShowParameters();
		map.put("show", "true");
		return map;
	}

	@Override
	public String getInitializationFunctionName() {
		return "CFW_Popover";
	}

}
