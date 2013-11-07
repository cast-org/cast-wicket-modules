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
package org.cast.cwm.glossary;

import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.xml.IDocumentObserver;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GlossaryXmlDocumentObserver  implements IDocumentObserver {
	
	private static final long serialVersionUID = 1L;

	Glossary glossary;
	
	// XML Tag Definitions
	public static final String ENTRY_ELEMENT = "level1";
	public static final String HEADWORD_ELEMENT = "h1";
	public static final String TERMS_ELEMENT = "list";
	public static final String DEFINITION_ELEMENT = "level2";
	public static final String ID_ATTRIBUTE = "id";
	
	final private static Logger log = LoggerFactory.getLogger(GlossaryXmlDocumentObserver.class);

	public GlossaryXmlDocumentObserver(Glossary glossary) {
		this.glossary = glossary;
	}

	/** 
	 * Make the list of entries consistent with the XML file.
	 * This method is part of the IDocumentObserver interface and will be automatically
	 * called when the Glossary object is created or the XML is modified.
	 */
	@Override
	public void xmlUpdated(XmlDocument doc) {
		glossary.setLanguage(doc.getDocument().getDocumentElement().getAttributeNS(null, "xml:lang"));
		createEntriesFromXmlSections(doc.getTocSection().getChildren());
	}
	
	/**
	 * Create {@link IGlossaryEntry} objects from this document.  This also checks the processed
	 * entries against a list of existing entries from {@link GlossaryService#getEntryMap()}
	 * and adds them using {@link GlossaryService#saveEntry(IGlossaryEntry)}.
	 * 
	 * @param doc
	 * @param dbEntries
	 */
	private void createEntriesFromXmlSections(List<XmlSection> sections) {
		for (XmlSection sec : sections)
			parseElement(sec);
	}

	/**
	 * Examine the given XmlSection; if it represents an Entry, then 
	 * add that Entry to this Glossary; otherwise drill down into the 
	 * XmlSection's children, if any.
	 * 
	 * During this process, {@link #parseElement(XmlSection, Map)} will check
	 * entries against an existing list of database entries.  
	 * 
	 * @param section
	 */
	private void parseElement (XmlSection sec) {
		if (ENTRY_ELEMENT.equals(sec.getElement().getLocalName())) {
			try {
				IModel<? extends IGlossaryEntry> e = getEntryFromSection(sec);
				glossary.addEntry(e);
			} catch (IllegalArgumentException e) {
				// Don't throw a fatal error if XML is unparsable.
				log.error("Malformed glossary entry in XML: {}", e);
			}
			
		} else {
			// Not an entry, but perhaps my children are.
			for (XmlSection child : sec.getChildren()) 
				parseElement(child);
		}
	}
	
	
	/**
	 * Creates a glossary {@link IGlossaryEntry} from an {@link XmlSection}.
     * In addition, it searches the XML for alternative
     * terms that map to this Entry and adds them to the {@link #termMap}.
	 * 
	 * @param sec an XMLSection
	 * @return the created glossary entry, whose class is specified by GlossaryService
	 */
	public IModel<? extends IWritableGlossaryEntry> getEntryFromSection(XmlSection sec) {

		IModel<? extends IWritableGlossaryEntry> eModel = GlossaryService.get().newEntryModel();
		IWritableGlossaryEntry e = eModel.getObject();
		e.setIdentifier(sec.getElement().getAttributeNS(null, ID_ATTRIBUTE));
		e.setXmlPointer(new XmlSectionModel(sec));
		
		sec.getElement().normalize();
		NodeList children = sec.getElement().getChildNodes();
		
		boolean foundShortDef = false;
		
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals(HEADWORD_ELEMENT)) {
				String headword = child.getTextContent();
				e.setHeadword(glossary.normalizeHeadword(headword));
				e.setSortForm(glossary.normalizeTerm(headword));
				e.addAlternateForm(glossary.normalizeTerm(headword));
			} else if(child.getNodeName().equals(TERMS_ELEMENT)) {
				addTerms(e, (Element)child);
			} else if (child.getNodeName().equals(DEFINITION_ELEMENT) && !foundShortDef) {
				NodeList grandKids = child.getChildNodes();
				for (int j = 0; j < grandKids.getLength() && !foundShortDef; j++) {
					Node grandKid = grandKids.item(j);
					if (grandKid.getNodeName().equalsIgnoreCase("p")) {
						e.setShortDef(grandKid.getTextContent());
						foundShortDef = true;
					}
				}
			}
		}

		if (Strings.isEmpty(e.getHeadword())) {
			throw new IllegalArgumentException("XML Does not contain a glossary word name");
		}

		return eModel;
	}

	/** Add the text content of each child of the given element as a Term.
	 * 
	 * @param element - XML element that holds a list of terms.
	 */
	private void addTerms(IWritableGlossaryEntry entry, Element element) {
		
		NodeList children = element.getChildNodes();
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				String normalizedTerm = glossary.normalizeTerm(child.getTextContent());
				if (!Strings.isEmpty(normalizedTerm))
					entry.addAlternateForm(normalizedTerm);
			}
		}
	}

}
