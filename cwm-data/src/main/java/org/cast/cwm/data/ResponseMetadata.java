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
package org.cast.cwm.data;

import com.google.inject.Inject;
import lombok.Data;
import org.apache.wicket.injection.Injector;
import org.cast.cwm.service.IUserContentService;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All the data the application needs to know from the XML to set up a response area.
 * @author bgoldowsky
 *
 */
@Data
public class ResponseMetadata implements Serializable {

	protected String id;

	protected String collection = null;
	
	/** Map of metadata for each response type.  
	 * Map key is the string name of the IContentType object (eg "SVG") rather than the IContentType enum object itself
	 * to facilitate use in PropertyModel 
	 * such as {@code  PropertyModel(responseMetadata, "type.SVG.preferred")}
	 */
	protected Map<String,TypeMetadata> typeMap;
	
	@Inject
	protected IUserContentService typeRegistry;

	public ResponseMetadata () {
		super();
		Injector.get().inject(this);
	}
	
	/** 
	 * Construct by extracting information from XML <responsegroup> element.
	 * @param elt
	 */
	public ResponseMetadata (Element elt) {
		this();
		if (!elt.getLocalName().equals("responsegroup"))
			throw new IllegalArgumentException("ResponseMetadata must be initialized with a responsegroup node");
		
		id = elt.getAttributeNS(null, "id");
		
		if (elt.hasAttribute("group")) {
			if (!elt.getAttributeNS(null, "group").trim().isEmpty())
				collection = elt.getAttributeNS(null, "group").trim();
		}
		
		// TODO Consider extracting attributes class, title
		// TODO Consider extracting elements prompt, annotation, select, select1, clozepassage
			
		typeMap = new HashMap<String,TypeMetadata>(6);
		
		NodeList resplist = elt.getElementsByTagNameNS(elt.getNamespaceURI(), "response");
		for (int i=0; i<resplist.getLength(); i++) {
			Element relt = (Element) resplist.item(i);
			String type = relt.getAttributeNS(null, "type");

			TypeMetadata typeMD = new TypeMetadata();
			
			if (relt.getAttributeNS(null, "preferred").equalsIgnoreCase("true"))
				typeMD.preferred = true;
			
			// TODO consider extracting attributes width, height
			
			typeMD.templates = new ArrayList<String>(6);
			NodeList templates = relt.getElementsByTagNameNS(elt.getNamespaceURI(), "template");
			for (int j=0; j<templates.getLength(); j++)
				typeMD.templates.add(templates.item(j).getTextContent().trim());

			typeMD.fragments = new ArrayList<String>(6);
			NodeList starters = relt.getElementsByTagNameNS(elt.getNamespaceURI(), "fragment");
			for (int j=0; j<starters.getLength(); j++)
				typeMD.fragments.add(normalizeWhitespace(starters.item(j).getTextContent()));
			
			// FIXME - remove interactive applet?
			// determine a way for this to be set to the default response types set by the application  - ldm			
			if (type.equals("text"))
				typeMap.put("HTML", typeMD);
			else if (type.equals("image"))
				typeMap.put("SVG", typeMD);
			else if (type.equals("audio"))
				typeMap.put("AUDIO", typeMD);
			else if (type.equals("file"))
				typeMap.put("UPLOAD", typeMD);
			else if (type.equals("table"))
				typeMap.put("TABLE", typeMD);
			else if (type.equals("applet"))
				typeMap.put("APPLET", typeMD);
			else
				throw new IllegalArgumentException("Unknown response type in XML: " + type);				
		}
	}
	
	/**
	 * Convenience method for getting metadata related to a given IContentType.
	 * @param type
	 * @return the TypeMetadata for the given type.
	 */
	public TypeMetadata getType (IContentType type) {
		return typeMap.get(type.name());
	}
	
	/**
	 * Get TypeMetadata for the named response type.
	 * @param typeName
	 * @return the TypeMetadata for the given type.
	 */
	public TypeMetadata getType (String typeName) {
		return typeMap.get(typeName);
	}
	
	/**
	 * Make sure the given IContentType is included in this metadata object, and return it.
	 * @param type
	 * @return the TypeMetadata for the given type
	 */
	public TypeMetadata addType (IContentType type) {
		if (typeMap == null)
			typeMap = new HashMap<String,TypeMetadata>(4);
		TypeMetadata typeMetadata = getType(type);
		if (typeMetadata == null) {
			typeMetadata = new TypeMetadata();
			typeMap.put(type.name(), typeMetadata);
		}
		return typeMetadata;
	}
	
	/**
	 * Trim string and collapse any sequences of spaces or other whitespace into just one space.
	 * This cleans up strings from XML which may have arbitrary whitespace inserted.
	 * @return the string with cleaned-up whitespace.
	 */
	private String normalizeWhitespace (String s) {
		return s.replaceAll("\\s+", " ").trim();
	}
	
	@Data
	public static class TypeMetadata implements Serializable {
		
		boolean preferred = false;
		
		List<String> templates;
		
		List<String> fragments;
		
		private static final long serialVersionUID = 1L;
	}
	
}