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
package org.cast.cwm.xml.parser;

import lombok.Getter;
import lombok.Setter;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;

import java.io.InputStream;
import java.util.Map;

/**
 * Parses XML input and returns a root {@link XmlSection} for the parsed tree.  The
 * scope of XmlSections is determined by the implementation.
 * 
 * @author jbrookover
 *
 */
public abstract class XmlParser {
	
	@Getter @Setter protected Map<String, XmlSection> idMap;
	@Getter @Setter protected XmlDocument doc;
	
	public XmlParser() {
	}

	protected void addToIdMap(XmlSection sec) {
		addToIdMap(null, sec);
	}
	
	protected void addToIdMap(String id, XmlSection sec) {
		if (idMap != null)
			idMap.put(id == null ? sec.getId() : id, sec);
	}
	
	public abstract XmlSection parse(InputStream in);
	/**
	 * Parse the XML input and return the root section.  Also stores a map of
	 * ID -> Section in the provided map for quick access later on.
	 * 
	 * @param in the XML stream
	 * @param idMap the map where the IDs should be stored
	 * @return the root section
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */

}
