/*
 * Copyright 2011-2014 CAST, Inc.
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
package org.cast.cwm.xml;

import java.io.Serializable;

import lombok.Getter;

import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * Holds both the Element and serialized String form of the result of a transformation.
 * The serialized string version will be created as necessary and then cached.
 * 
 * @author borisgoldowsky
 */
public class TransformResult implements Serializable {

	@Getter
	protected Element element;
	
	protected String string;
	
	private static final long serialVersionUID = 1L;

	public TransformResult (Element element) {
		this.element = element;
		this.string = null;
	}
	
	public String getString() {
		if (string == null && element != null) {
			string = serialize(element);
		}
		return string;
	}
	
	/**
	 * @param res
	 * @return
	 * 
	 * This will return a string of the elements children.  Can be used to get the html content from a child/children.
	 */
	public String serialize (Element res) {
		DOMImplementationRegistry registry;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		DOMImplementationLS impl = 
			(DOMImplementationLS)registry.getDOMImplementation("LS");
		if (impl == null) {
			throw new RuntimeException ("Could not find appropriate DOM implementation");
		} else {
			LSSerializer writer = impl.createLSSerializer();
			writer.getDomConfig().setParameter("xml-declaration", false);
			// May want to set a filter to deal with Components embedded in XML
			// writer.setFilter(filter);
			return writer.writeToString(res);
		}
	}

}
