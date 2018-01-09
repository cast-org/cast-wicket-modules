/*
 * Copyright 2011-2018 CAST, Inc.
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
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Map;

import static org.cast.cwm.figuration.hideable.FigurationModal.BackdropType.ACTIVE;
import static org.cast.cwm.figuration.hideable.FigurationModal.BackdropType.STATIC;

/**
 * Base class for modal dialogs built with Figuration.
 *
 * For very simple modals, this class can be used directly, with a Panel passed in as the body:
 * <code><pre>
 *     new FigurationModal&lt;Void>("modal")
 *         .withTitle("Modal Title")
 *         .withBody(new MyBodyPanel(FigurationModal.BODY_ID))
 *         .withEmptyFooter();
 * </pre></code>
 *
 * Wicket IDs of the header, body, and footer are set in the markup for this class;
 * you can use the defined constants for them as in the example above.
 * Components for each must be supplied, or explicitly set to be empty.
 *
 * Alternatively, provide your own markup and avoid these restrictions.
 * For modals that include behavior, buttons in the footer, etc, you will usually want to do this.
 * You still have to make sure the markup includes divs with the correct class attributes,
 * just like the markup for this class, since that's how Figuration attaches its styling and behavior.
 *
 * Modals can be opened and closed in various ways:  by client side Javascript, by
 * {@link FigurationTriggerBehavior}, or as part of an AJAX request using
 * {@link FigurationHideable#show(AjaxRequestTarget)} or
 * {@link FigurationHideable#connectAndShow(Component, AjaxRequestTarget)}
 *
 * This class does not do any event logging itself, but you can easily log an event when a dialog is
 * opened or closed (or any other supported event) using org.cast.cwm.data.behavior.EventLoggingBehavior, eg:
 * <code><pre>modal.add(new EventLoggingBehavior("beforeShow.cfw.modal", eventType));</pre></code>
 *
 * @author bgoldowsky
 *
 */
public class FigurationModal<T> extends FigurationHideable<T> {

	/**
	 * The backdrop is the grey overlay that dims the part of the page that's not the modal.
	 * BackdropType NONE means that no backdrop should be added.
	 * STATIC is a simple, non-clickable backdrop.
	 * ACTIVE (the default) is a backdrop that closes the modal when clicked.
	 */
	public enum BackdropType { NONE, STATIC, ACTIVE }

	@Getter @Setter
	protected BackdropType backdrop = ACTIVE;


	public FigurationModal(String id) {
		this(id, null);
	}

	public FigurationModal(String id, IModel<T> model) {
		super(id, model);
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
		add(new FigurationModalBasicHeader(HEADER_ID, mTitle));
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

	@Override
	public Map<String, String> getInitializeParameters() {
		Map<String, String> map = super.getInitializeParameters();
		// active backdrop is the default.
		if (backdrop != ACTIVE)
			map.put("backdrop", backdrop==STATIC ? "static" : "false");
		return map;
	}

	@Override
	public String getInitializationFunctionName() {
		return "CFW_Modal";
	}

	@Override
	public String getClassAttribute() {
		return "modal";
	}

}
