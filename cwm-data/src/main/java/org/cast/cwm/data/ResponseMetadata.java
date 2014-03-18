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
package org.cast.cwm.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.apache.wicket.injection.Injector;
import org.cast.cwm.IResponseTypeRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;

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
	
	@Inject
	protected IResponseTypeRegistry typeRegistry;

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
	 * Convenience method for getting metadata related to a given ResponseType.
	 * @param type
	 * @return the TypeMetadata for the given type.
	 */
	public TypeMetadata getType (IResponseType type) {
		return typeMap.get(type.getName());
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
	 * Make sure the given ResponseType is included in this metadata object, and return it.
	 * @param type
	 * @return the TypeMetadata for the given type
	 */
	public TypeMetadata addType (IResponseType type) {
		if (typeMap == null)
			typeMap = new HashMap<String,TypeMetadata>(4);
		TypeMetadata typeMetadata = getType(type);
		if (typeMetadata == null) {
			typeMetadata = new TypeMetadata();
			typeMap.put(type.getName(), typeMetadata);
		}
		return typeMetadata;
	}
	
	/**
	 * Make sure the reponse type with the given name is included in this metadata object,
	 * and return it.
	 * @param typeName
	 * @return the TypeMetadata for the given type
	 */
	public TypeMetadata addType (String typeName) {
		return addType (typeRegistry.getResponseType(typeName));
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