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
package org.cast.cwm.data.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * Disable the provided components during an ajax call.  This decorator will remove all 'click' type
 * event handlers from the given components, and their children, during the Ajax request.  Each component
 * must have {@link Component#setOutputMarkupId(boolean)} set to 'true' or an exception will be thrown.
 * 
 * @author jbrookover
 *
 */
public class DisablingAjaxCallDecorator implements IAjaxCallDecorator {

	private static final long serialVersionUID = 1L;
	private static final PackageResourceReference blockingJavascriptReference = 
		new PackageResourceReference(DisablingAjaxCallDecorator.class, "disablingAjaxCallDecorator.js");

	private String selector;
	private List<Component> blockedComponents;
	
	public static PackageResourceReference getJSResourceReference() {
		return blockingJavascriptReference;
	}
	
	public DisablingAjaxCallDecorator(Collection<? extends Component> components ) {
		blockedComponents = new ArrayList<Component>();
		for (Component c : components) {
			if (!c.getOutputMarkupId())
				throw new IllegalArgumentException("Disabled components must have a markup ID");
			blockedComponents.add(c);
		}
	}
	
	public CharSequence decorateScript(Component component, CharSequence script) {
		return "$('" + generateSelector() + "').ajaxDisable(); " + script;
	}

	public CharSequence decorateOnFailureScript(Component component,CharSequence script) {
		return "$('" + generateSelector() + "').ajaxEnable(); " + script;
	}

	public CharSequence decorateOnSuccessScript(Component component,CharSequence script) {
		return "$('" + generateSelector() + "').ajaxEnable(); " + script;
	}
	
	private String generateSelector() {
		if (selector == null) {
			StringBuffer buffer = new StringBuffer();
			for(Component c : blockedComponents) {
				buffer.append("#" + c.getMarkupId() + ", ");
			}
			if(buffer.length() > 0)
				buffer.deleteCharAt(buffer.lastIndexOf(","));
			selector = buffer.toString().trim();
		}
		return selector;
	}

}
