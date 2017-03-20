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

import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.figuration.behavior.ModalTriggerBehavior;

import java.util.Map;

/**
 * Base class for modal dialogs built with Figuration.
 *
 * For very simple modals, this class can be used directly, with a Panel passed in as the body:
 * <code><pre>
 *     new FigurationModal&lt;Void>("modal")
 *         .withTitle("Modal Title")
 *         .withBody(bodyPanel)
 *         .withEmptyFooter();
 * </pre></code>
 *
 * Wicket IDs of the header, body, and footer are set by this class to be "header", "body", and "footer".
 *
 * For modals that include behavior, buttons in the footer, etc, you will usually want to override this
 * class and provide your own markup.  Make sure the markup includes divs with the correct class attributes,
 * just like the markup for this class.
 *
 * Modals can be opened and closed in various ways:  by client side Javascript, by a component with a
 * {@link ModalTriggerBehavior} attached, or as part of an AJAX request using
 * {@link FigurationHideable#show(Component, AjaxRequestTarget)}
 *
 * This class does not do any event logging itself, but you can easily log an event when a dialog is
 * opened or closed (or any other supported event) using org.cast.cwm.data.behavior.EventLoggingBehavior, eg:
 * <code><pre>modal.add(new EventLoggingBehavior("beforeShow.cfw.modal", eventType));</pre></code>
 *
 * @author bgoldowsky
 *
 */
public class FigurationModal<T> extends FigurationHideable<T> {

	public static final String HEADER_ID = "header";
	public static final String BODY_ID = "body";
	public static final String FOOTER_ID = "footer";

	public FigurationModal(String id) {
		this(id, null);
		addClassAttributeModifier();
	}

	public FigurationModal(String id, IModel<T> model) {
		super(id, model);
	}

	/**
	 * Sets up a ClassAttributeModifier that adds the figuration-required class attribute to the top level tag.
	 */
	protected void addClassAttributeModifier() {
		add(ClassAttributeModifier.append("class", "modal"));
	}

	/**
	 * Adds a simple header to the modal with the given title and a close button.
	 *
	 * @param title Title that will be shown in the header of the modal.
	 * @return this, for chaining
	 */
	public FigurationModal<T> withTitle(String title) {
		return withTitle(Model.of(title));
	}

	/**
	 * Adds a simple header to the modal with the given title and a close button.
	 *
	 * @param mTitle Model of the title that will be shown in the header of the modal.
	 * @return this, for chaining
	 */
	public FigurationModal<T> withTitle(IModel<String> mTitle) {
		add(new FigurationModalBasicHeader("header", mTitle));
		return this;
	}

	/**
	 * Adds the given Panel as the header of this modal.
	 *
	 * @param headerPanel panel to use as the header
	 * @return this, for chaining
	 */
	public FigurationModal<T> withHeader(Panel headerPanel) {
		if (!headerPanel.getId().equals(HEADER_ID))
			throw new IllegalArgumentException("Header panel must have id " + HEADER_ID);
		addOrReplace(headerPanel);
		return this;
	}

	/**
	 * Adds the given Panel as the body of this modal.
	 *
	 * @param bodyPanel panel to use as the main body of the modal
	 * @return this, for chaining
	 */
	public FigurationModal<T> withBody(Panel bodyPanel) {
		if (!bodyPanel.getId().equals(BODY_ID))
			throw new IllegalArgumentException("Body panel must have id " + BODY_ID);
		addOrReplace(bodyPanel);
		return this;
	}

	/**
	 * Adds the given Panel as the footer of this modal.
	 * The footer is usually where any action or "OK" buttons would go.
	 *
	 * @param footerPanel panel to use as the footer of the modal
	 * @return this, for chaining
	 */
	public FigurationModal<T> withFooter(Panel footerPanel) {
		if (!footerPanel.getId().equals(FOOTER_ID))
			throw new IllegalArgumentException("Body panel must have id " + FOOTER_ID);
		addOrReplace(footerPanel);
		return this;
	}

	/**
	 * Specify that this modal will have no footer.
	 * An empty panel is added instead of any content.
	 *
	 * @return this, for chaining
	 */
	public FigurationModal<T> withEmptyFooter() {
		return withFooter(new EmptyPanel(FOOTER_ID));
	}

	// FIXME: explain
	@Override
	protected Map<String, String> getShowParameters() {
		Map<String, String> map = super.getShowParameters();
		map.put("show", "true");
		return map;
	}

	@Override
	public String getInitializationFunctionName() {
		return "CFW_Modal";
	}
	
}
