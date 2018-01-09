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
package org.cast.cwm.data.component;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * Disables some set of components during an AJAX call.  
 * This listener will call Javascript to remove all 'click' type
 * event handlers from the given components, and their children, during the Ajax request.  Each component
 * must have {@link Component#setOutputMarkupId(boolean)} set to 'true' or an exception will be thrown.
 * 
 * @author jbrookover
 * @author bgoldowsky (rewrote as AjaxCallListener for wicket 6)
 */

public class DisablingAjaxCallListener extends AjaxCallListener implements IComponentAwareHeaderContributor {

	private static final long serialVersionUID = 1L;

	private String selector;

	private static final PackageResourceReference blockingJavascriptReference = 
			new PackageResourceReference(DisablingAjaxCallListener.class, "disablingAjaxCallDecorator.js");

	@Override
	public CharSequence getBeforeSendHandler(Component component) {
		return String.format("$('%s').ajaxDisable();", generateSelector(component));
	}

	@Override
	public CharSequence getCompleteHandler(Component component) {
		return String.format("$('%s').ajaxEnable();", generateSelector(component));
	}

 	public void renderHead(Component component, IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(blockingJavascriptReference));
 	}

	private String generateSelector(Component component) {
		if (selector == null) {
			if (component instanceof IDisablingComponent) {
				Collection<? extends Component> blockedComponents = ((IDisablingComponent) component).getComponents();
				StringBuffer buffer = new StringBuffer();
				for(Component c : blockedComponents) {
					if (!c.getOutputMarkupId())
						throw new IllegalArgumentException("Disabled components must have a markup ID");
					buffer.append("#" + c.getMarkupId() + ", ");
				}
				if(buffer.length() > 0)
					buffer.deleteCharAt(buffer.lastIndexOf(","));
				selector = buffer.toString().trim();
			} else {
				throw new IllegalStateException("This listener should only be attached to components that implement IDisablingComponent");
			}
		}
		return selector;
	}

}

