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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

/**
 * Delete when references in ISI are removed.
 * 
 * @author jbrookover
 *
 */
@Deprecated
public class ModalLink extends AjaxLink {
 
  private static final long serialVersionUID = 1L;
  private ModalWindow window;

  /**
   * shows a modal window when clicked
   * @param id - the wicket id
   * @param window - the modal window to show when this link is clicked (not null)
   * @throws IllegalArgumentException if window is null
   */
  public ModalLink(String id, ModalWindow window) throws IllegalArgumentException {
    super(id);
    if(window == null)
      throw new IllegalArgumentException("the modal window cannot be null");
    this.window = window;
    setOutputMarkupId(true);
  }

  @Override
  public void onClick(AjaxRequestTarget target) {
    if(getPage() != null) {
      getPage().visitChildren(IModalEventListener.class, new IVisitor<Component>() {
  
        public Object component(Component component) {
          ((IModalEventListener)component).onShow();
          return CONTINUE_TRAVERSAL;
        }
        
      });
    }
    window.show(target); 
  }
}
