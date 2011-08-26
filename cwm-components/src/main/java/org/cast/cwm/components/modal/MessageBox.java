/*
 * Copyright 2011 CAST, Inc.
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
package org.cast.cwm.components.modal;

import org.apache.wicket.Resource;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Delete when references in ISI are removed.
 * @author jbrookover
 *
 */
@Deprecated
public class MessageBox extends Panel {
	private static final long serialVersionUID = 1L;
	public MessageBox(String id, String message, final Resource button1, final Resource button2, final ModalMessageBox box) {
		super(id);
		add(new Label("message", message));
		add(new AjaxLink<Object>("button1") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				box.setClickedButton(ModalMessageBox.Button.button1);
				box.close(target);
			}
			@Override
			public boolean isVisible() {
			  return button1 != null;
			}
		}.add(new Image("button1Image", button1)));
		add(new AjaxLink<Object>("button2") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				box.setClickedButton(ModalMessageBox.Button.button2);
				box.close(target);
			}
			@Override
			public boolean isVisible() {
			  return button2 != null;
			}
		}.add(new Image("button2Image", button2)));
	}
}
