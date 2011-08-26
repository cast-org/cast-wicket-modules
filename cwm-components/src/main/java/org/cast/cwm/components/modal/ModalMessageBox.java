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

import org.apache.wicket.Component;
import org.apache.wicket.Resource;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

/**
 * Delete when references in ISI are removed.
 */
@SuppressWarnings("serial")
@Deprecated
public abstract class ModalMessageBox extends ModalWindow {
	
	public enum Button {button1, button2, close};
	private Button button;
	
	/**
	 * @param id - the wicket markup id
	 * @param header - the title to be shown
	 * @param message - the message to be displayed
	 * @param button1 - the first button
	 * @param button2 - the second button
	 */
	public ModalMessageBox(String id, String header, String message, Resource button1, Resource button2) {
		this(id, header, message, "", button1, button2);
	}
	/**
	 * @param id - the wicket markup id
	 * @param header - the title to be shown
	 * @param message - the message to be displayed
	 * @param cssClass - the css class name to use
	 * @param button1 - the first button
	 * @param button2 - the second button
	 */
	public ModalMessageBox(String id, String header, String message, String cssClass, Resource button1, Resource button2) {
		super(id);
		setOutputMarkupId(true);
		setContent(new MessageBox(getContentId(), message, button1, button2, this));
		setTitle(header);
		// setCookieName("modal");
		setMinimalHeight(100);
		setMinimalWidth(400);
		setInitialHeight(100);
		setInitialWidth(400);
		if(!cssClass.equals(""))
			setCssClassName(cssClass);
		else
			setCssClassName(ModalWindow.CSS_CLASS_GRAY);
		setCloseButtonCallback(new CloseButtonCallback() {
			private static final long serialVersionUID = 1L;
			public boolean onCloseButtonClicked(AjaxRequestTarget target) {
				button = Button.close;
				return true;
			}});
		setWindowClosedCallback(new WindowClosedCallback() {
			private static final long serialVersionUID = 1L;
			public void onClose(AjaxRequestTarget target) {
				onClosed(target);
			}		
		});
	}
	public void setClickedButton(Button button) {
		this.button = button;
	}
	public Button getClickedButton() {
		return button;
	}
	
	@Override
	public void close(AjaxRequestTarget target) {
	  if(getPage() != null) {
      getPage().visitChildren(IModalEventListener.class, new IVisitor<Component>() {
  
        public Object component(Component component) {
          ((IModalEventListener)component).onHide();
          return CONTINUE_TRAVERSAL;
        }
        
      });
    }
	  super.close(target);
	}

	public abstract void onClosed(AjaxRequestTarget target);
}