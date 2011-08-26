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
package org.cast.cwm.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * All the data the application needs to know from the XML to set up a response area.
 * @author bgoldowsky
 *
 */
@Data
public class ResponseMetadata implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected String id;

	protected String collection = null;
	
	/** Map of metadata for each response type.  
	 * Map key is the string value of the ResponseType object (eg "SVG") rather than the ResponseType enum object itself
	 * to facilitate use in PropertyModel 
	 * such as {@code  PropertyModel(responseMetadata, "type.SVG.preferred")}
	 */
	protected Map<String,TypeMetadata> typeMap;
	
	public ResponseMetadata () {
	}
	
	/** 
	 * Construct by extracting information from XML <responsegroup> element.
	 * @param elt
	 */
	public ResponseMetadata (Element elt) {
		
		if (!elt.getLocalName().equals("responsegroup"))
			throw new IllegalArgumentException("ResponseMetadata must be initialized with a responsegroup node");
		
		id = elt.getAttribute("id");
		
		if (elt.hasAttribute("group")) {
			if (!elt.getAttribute("group").trim().isEmpty())
				collection = elt.getAttribute("group").trim();
		}
		
		// TODO Consider extracting attributes class, title
		// TODO Consider extracting elements prompt, annotation, select, select1, clozepassage
			
		typeMap = new HashMap<String,TypeMetadata>(4);
		
		NodeList resplist = elt.getElementsByTagNameNS(elt.getNamespaceURI(), "response");
		for (int i=0; i<resplist.getLength(); i++) {
			Element relt = (Element) resplist.item(i);
			String type = relt.getAttribute("type");

			TypeMetadata typeMD = new TypeMetadata();
			
			if (relt.getAttribute("preferred").equalsIgnoreCase("true"))
				typeMD.preferred = true;
			
			// TODO consider extracting attributes width, height
			
			typeMD.templates = new ArrayList<String>(4);
			NodeList templates = relt.getElementsByTagNameNS(elt.getNamespaceURI(), "template");
			for (int j=0; j<templates.getLength(); j++)
				typeMD.templates.add(templates.item(j).getTextContent().trim());

			typeMD.fragments = new ArrayList<String>(4);
			NodeList starters = relt.getElementsByTagNameNS(elt.getNamespaceURI(), "fragment");
			for (int j=0; j<starters.getLength(); j++)
				typeMD.fragments.add(starters.item(j).getTextContent().trim());
			
			if (type.equals("text"))
				typeMap.put(ResponseType.HTML.name(), typeMD);
			else if (type.equals("image"))
				typeMap.put(ResponseType.SVG.name(), typeMD);
			else if (type.equals("audio"))
				typeMap.put(ResponseType.AUDIO.name(), typeMD);
			else if (type.equals("file"))
				typeMap.put(ResponseType.UPLOAD.name(), typeMD);
			else
				throw new IllegalArgumentException("Unknown response type in XML: " + type);
				
		}
	}
	
	/**
	 * Convenience method for getting metadata related to a given ResponseType.
	 * @param type
	 * @return the TypeMetadata for the given type.
	 */
	public TypeMetadata getType (ResponseType type) {
		return typeMap.get(type.name());
	}
	
	/**
	 * Make sure the given ResponseType is included in this metadata object, and return it.
	 * @param type
	 * @return the TypeMetadata for the given type
	 */
	public TypeMetadata addType (ResponseType type) {
		if (typeMap == null)
			typeMap = new HashMap<String,TypeMetadata>(4);
		TypeMetadata typeMetadata = getType(type);
		if (typeMetadata == null) {
			typeMetadata = new TypeMetadata();
			typeMap.put(type.name(), typeMetadata);
		}
		return typeMetadata;
	}
	
	@Data
	public static class TypeMetadata implements Serializable {
		
		boolean preferred = false;
		
		List<String> templates;
		
		List<String> fragments;
		
		private static final long serialVersionUID = 1L;
	}
	
}
