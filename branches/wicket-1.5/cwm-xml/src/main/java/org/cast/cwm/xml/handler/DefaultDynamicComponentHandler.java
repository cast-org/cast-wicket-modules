/*
 * Copyright 2011-2015 CAST, Inc.
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
package org.cast.cwm.xml.handler;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.cast.cwm.IRelativeLinkSource;
import org.cast.cwm.xml.IXmlPointer;
import org.cast.cwm.xml.service.IXmlService;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/*
 * Copyright 2011-2013 CAST, Inc.
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
import com.google.inject.Inject;

/**
 * A Component Handler to be used as a last ditch if no others applied.
 * 
 * @see IDynamicComponentHandler
 * 
 * @author droby
 *
 */
public class DefaultDynamicComponentHandler implements IDynamicComponentHandler {

	@Inject
	IXmlService xmlService;
	
	public DefaultDynamicComponentHandler() {
		super();
		Injector.get().inject(this);
	}

	public Component getComponent(String wicketId, Element element,
			IRelativeLinkSource linkSource, IModel<? extends IXmlPointer> secMod ) {
		boolean isContainer = false;
		
		// Check to see if this element has any Wicket Children.
		NodeList childrenComponents = xmlService.getWicketNodes(element, false);
		if (childrenComponents.getLength() > 0) {
			isContainer = true;
		}
		
		// If we found a child, return a container with debugging style.  Otherwise, a generic label.
		if (isContainer) {
			return new WebMarkupContainer(wicketId).add(AttributeModifier.replace("style", "border: 3px solid red"));
		} else {
			return new Label(wicketId, "[[[Dynamic component with ID " + wicketId + "]]]").add(AttributeModifier.replace("style", "border: 3px solid red"));
		}
	}

	public boolean canHandle(String wicketId) {
		return true;
	}

}
