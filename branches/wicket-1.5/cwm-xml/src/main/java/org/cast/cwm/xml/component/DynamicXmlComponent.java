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
package org.cast.cwm.xml.component;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.cast.cwm.IInputStreamProvider;
import org.cast.cwm.IRelativeLinkSource;
import org.cast.cwm.xml.ICacheableModel;
import org.cast.cwm.xml.IXmlPointer;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.handler.IDynamicComponentHandler;
import org.w3c.dom.Element;

import com.google.inject.Inject;

/**
 * An extension of XmlComponent using an injected IDynamicComponentResolver to 
 * build the component hierarchy.  This could be merged with XMLComponent itself, 
 * but is provided separately as it might ease conversion from the old way of 
 * extending XmlComponent in the assorted applications.
 * 
 * @see IDynamicComponentHandler, IDynamicComponentResolver
 * 
 * @author droby
 *
 */
public class DynamicXmlComponent extends XmlComponent {

	private static final long serialVersionUID = 1L;

	@Inject
	protected IDynamicComponentResolver resolver;

	public DynamicXmlComponent(String id,
			ICacheableModel<? extends IXmlPointer> secMod, String transformName) {
		super(id, secMod, transformName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getDynamicComponent(String wicketId, Element elt) {
	
		IDynamicComponentHandler handler = resolver.getHandler(wicketId);
		return handler.getComponent(wicketId, 
				elt, 
				getLinkSource(), 
				(IModel<? extends IXmlPointer>) getDefaultModel());
	}

	protected IRelativeLinkSource getLinkSource() {
		IXmlPointer pointer = getModel().getObject();
		if (!(pointer instanceof XmlSection))
			throw new IllegalStateException("Can't find file reference for xml element");
		IInputStreamProvider xmlFile = ((XmlSection)pointer).getXmlDocument().getXmlFile();
		if (!(xmlFile instanceof IRelativeLinkSource))
			throw new IllegalStateException("Can't find reference relative to file " + xmlFile);
	
		return (IRelativeLinkSource) xmlFile;
	}

}