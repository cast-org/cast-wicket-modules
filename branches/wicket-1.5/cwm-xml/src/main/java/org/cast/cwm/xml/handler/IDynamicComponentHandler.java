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
package org.cast.cwm.xml.handler;

import org.apache.wicket.Component;
import org.cast.cwm.IRelativeLinkSource;
import org.cast.cwm.xml.component.IDynamicComponentResolver;
import org.w3c.dom.Element;

/**
 * A handler for an xml fragment.  Used in generating dynamic Wicket Components corresponding to xml fragments.
 * 
 * @see IDynamicComponentResolver
 * 
 * @author droby
 *
 */
public interface IDynamicComponentHandler {
	
	/**
	 * get Wicket Component for a given fragment identified by wicket Id
	 *
	 * @param wicketId wicket id for component
	 * @param element xml markup for component
	 * @param linkSource sometime needed for resolution of links relative to input xml.
	 * @return a Component with given wicket Id using the Xml of the element as markup.
	 */
	Component getComponent(String wicketId, Element element, IRelativeLinkSource linkSource);

	/**
	 * Is this the appropriate handler for a given fragment identified by wicketId?
	 * 
	 * @param wicketId
	 * @return true if can handle fragment, false otherwise 
	 */
	boolean canHandle(String wicketId);

}
