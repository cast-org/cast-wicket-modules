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
package org.cast.cwm.xml.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.wicket.injection.Injector;
import org.apache.xerces.util.XMLCatalogResolver;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.service.IXmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

/**
 * Parses an XML document using the DaisyBook format in the document
 * namespace http://www.daisy.org/z3986/2005/dtbook/. 
 * 
 * FIXME: probably should not be serializable (since XPathExpression isn't).  Figure out why some ISI pages want to serialize XmlSection.
 * 
 * @author jbrookover
 *
 */
public class DtbookParser extends XmlParser implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(DtbookParser.class);
	private static final String IDGENERATORSTRING = "gen"; 
	private transient XPathExpression idFinder = null;
	
	public DtbookParser () {
		super();
		Injector.get().inject(this);
	}
	
	/**
	 * An ID generator for each node.
	 */
	private int idSerial = 0;
	
	/**
	 * A counter to keep track of XmlSections as we come to them.  Used by XmlSection for ordering
	 */
	private int elementCounter = 0;
	
	@Inject
	private IXmlService xmlService;
	
	/** 
	 * Elements that are of interest for creating the document tree structure  
	 */
	protected static final Map<String,DtbookElement> elements = new HashMap<String,DtbookElement>();
	
	static {
		elements.put("dtbook", new DtbookElement(null, "book", false));
		elements.put("book", new DtbookElement("doctitle", "bodymatter", false));
		elements.put("bodymatter", new DtbookElement(null, "level1", true));
		
		// Standard Level Elements
		elements.put("level1", new DtbookElement("h1", "covertitle", "level2", true));
		elements.put("level2", new DtbookElement("h2", "covertitle", "level3", true));
		elements.put("level3", new DtbookElement("h3", "covertitle", "level4", true));
		elements.put("level4", new DtbookElement("h4", "covertitle", null, true));
	}

	/**
	 * Read the XML document associated with this object and set up all associated sections, ID cache, etc.
	 * 
	 * @param in
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@Override
	public XmlSection parse(InputStream in) {
		
		idSerial = 0;
		elementCounter = 0;

		// Locate the Dtbook XMLCatalogResolver
		URL catResource = DtbookParser.class.getClassLoader().getResource("cwm-xml-catalog.xml");
		if (catResource == null)
			throw new IllegalStateException("Cannot find XML Catalog");
		String [] catalogs = {catResource.toString()};
		XMLCatalogResolver resolver = new XMLCatalogResolver(catalogs);
		// Parse the XML stream
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setXIncludeAware(true);
		Document document;
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(resolver);
			document = db.parse(in);
		} catch (Exception e) {
			throw new RuntimeException("Couldn't parse XML Document", e);
		}
		if (document == null)
			return null;
		document.setXmlStandalone(false);
		
		// Create the Root XmlSection
		XmlSection root = xmlService.newXmlSection(); 
		root.init(doc, null, XmlSection.DOCUMENT_ID, document.getDocumentElement(), "DocumentTitle");

		addToIdMap(XmlSection.DOCUMENT_ID, root);
	
		// Recursively create child XmlSections
		fillIn(root, document.getDocumentElement());
		
		return root;
	}
	
	/**
	 * Fill in DtbookSpecific elements (title, class) in this XmlSection.  Then,
	 * process children of this section (if necessary).
	 * 
	 * @param section the section being processed, start with the root 
	 * @param elt the element that belongs to the section, start with the {@link Document#getDocumentElement()}
	 * @param idMap direct access idMap that each section is registered with
	 */
	private void fillIn(XmlSection section, Element elt) {
		
		// Add to the calling method's idMap, if applicable
		addToIdMap(section.getId(), section);

		DtbookElement thisElt = elements.get(elt.getLocalName());
		
		if (thisElt == null) {
			log.error ("Unexpected element type: {}", elt.getLocalName());
			return;
		}

		// Look for title and overwrite the default, if found.
		if (thisElt.titleElt != null) {
			Element child = getChildByLocalName(elt, thisElt.titleElt);
			if (child != null) {
				section.setTitle(normalizeTitle(child.getTextContent()));
			}
		}
		
		// Look for sub title
		// These are optional, so only look at direct children; don't allow descendants like getElementsByTagName does. 
		if (thisElt.subTitleElt != null) {
			Element child = getChildByLocalName(elt, thisElt.subTitleElt);
			if (child != null)
				section.setSubTitle(normalizeTitle(child.getTextContent()));
		}
		
		// Look for class attribute
		if (elt.hasAttribute("class"))
			section.setClassName(elt.getAttributeNS(null, "class"));
		
		// Set the order of this section in this document
		section.setSortOrder(elementCounter++);
		
		// Find all ID attributes that are descendants, and add to the ID Map.
		// (some of these may get reassigned later to children of this element)
		if (getIdMap() != null) {
			try {
				NodeList eltsWithIds = (NodeList) getIdFinder().evaluate(elt, XPathConstants.NODESET);
				for (int i=0; i<eltsWithIds.getLength(); i++) {
					String id = eltsWithIds.item(i).getTextContent();
					addToIdMap(id, section);
				}
			} catch (XPathExpressionException e) {
				throw new RuntimeException(e);
			}
		}
		
		// Stop Parsing if this element does not have a designated child element
		if (thisElt.childElt == null)
			return;
		
		// Get designated child elements, stop parsing if none found
		NodeList nl = elt.getElementsByTagName(thisElt.childElt);
		if (nl == null)
			return;

		// If tocLevel is false, this element has a single child that we fall through to
		// for information, without creating a new XmlSection
		// TODO: "TocLevel" is misleading, especially when the Root XmlSection object is called "Toc"
		if (!thisElt.tocLevel) {
			if (nl.getLength() != 1)
				throw new IllegalStateException("Expected Single child for Node, but found more than one: " + elt.getLocalName());
			fillIn(section, (Element)nl.item(0));
			return;
		} 
		
		// Finally, parse designated child elements and recursively create new XmlSections for each.
		for (int i=0; i<nl.getLength(); i++) {
			Element child = (Element) nl.item(i);
			// Make sure all elements we work with have IDs; generate if necessary
			String id = child.getAttributeNS(null, "id");
			if (id == null || id.equals("")) {
				id = IDGENERATORSTRING + String.valueOf(idSerial++);
				child.setAttribute("id", id);
			}
			XmlSection subsect = section.addChild(id, child, "Title Unknown");
			fillIn(subsect, child);
		}
	}

	/**
	 * Look for and return a direct child of an element with a given localname.
	 * @param element the parent element
	 * @param localName
	 * @return the child element, or null
	 */
	private Element getChildByLocalName (Element element, String localName) {
		NodeList nl = element.getChildNodes();
		for (int i=0; i<nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && localName.equals(node.getLocalName())) {
				return (Element) node;
			}
		}
		return null;
	}

	protected XPathExpression getIdFinder() {
		if (idFinder == null) {
			XPathFactory factory= XPathFactory.newInstance();
			XPath xPath=factory.newXPath();
			try {
				idFinder = xPath.compile(".//@id");
			} catch (XPathExpressionException e) {
				throw new RuntimeException("XPath syntax incorrect", e);
			}			
		}
		return idFinder;
	}
	

	
	private static class DtbookElement {
		String titleElt;
		String subTitleElt; // Optional
		String childElt;
		boolean tocLevel;
		
		DtbookElement (String title, String child, boolean tocLevel) {
			this(title, null, child, tocLevel);
		}
		
		DtbookElement (String title, String subTitle, String child, boolean tocLevel) {
			this.titleElt = title;
			this.subTitleElt = subTitle;
			this.childElt = child;
			this.tocLevel = tocLevel;	
		}
		
	}
	
	/**
	 * Normalization function run on text content of XmlSection titles.
	 * Removes leading and trailing whitespace, and replaces all
	 * other sequences of whitespace with single spaces.
	 * @param title
	 * @return
	 */
	protected String normalizeTitle (String title) {
		return title.replaceAll("\\s+", " ").trim();
	}
}
