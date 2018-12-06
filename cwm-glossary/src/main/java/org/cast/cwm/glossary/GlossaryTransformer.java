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
package org.cast.cwm.glossary;

import org.apache.wicket.util.time.Time;
import org.cast.cwm.xml.transform.IDOMTransformer;
import org.cast.cwm.xml.transform.TransformParameters;
import org.w3c.dom.*;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes an XML node and replaces in-text glossary words with 'glossary' nodes.  Each glossary word
 * is replaced by a &lt;gl&gt; element in the document's namespace.  The element will have
 * an "entryId" attribute that contains the ID of the glossary word.  The content inside the 
 * glossary element will remain the same.
 * <br /><br />
 * For example, if "gas" is a glossary word that is associated with "gaseous", as has the ID "g1",
 * the following would take place:
 * <pre>
 * Original Node: The gaseous cloud.
 * New Node: The &lt;gl entryId="g1"&gt;gaseous&lt;/gl&gt; cloud.
 * </pre> 
 * 
 * These can then be replaced during XSLT transform with appropriate links to the glossary page.
 * 
 * @author jbrookover
 *
 */
public class GlossaryTransformer implements IDOMTransformer, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * A list of elements that *should* be checked for glossary words.
	 */
	protected final List<String> glosswordCheckElements = 
		new ArrayList<String>(Arrays.asList("p", "em", "strong", "li", "span", "poem", "line"));
	
	/**
	 * A list of elements that will *not* be checked for glossary words.  Child elements
	 * of these elements will not be checked.
	 */
	protected final List<String> glosswordBlockingElements = 
		new ArrayList<String>(Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6", "hd", 
				"annotation", "sidebar"));
	
	/**
	 * A list of classes that will *not* be checked for glossary words.
	 * 
	 */
	protected final List<String> glosswordBlockingClasses =
		new ArrayList<String>(Arrays.asList("nolink"));

	/** 
	 * Any element with this class attribute will be linked to the glossary
	 * but will not be remembered as having been linked already.  As such, a
	 * glossary word could appear more than once on a given page if the 
	 * first occurrence happens in a section with this class attribute.
	 * 
	 */
	protected final List<String> glosswordLinkButNotRecordElements =
	    new ArrayList<String>(Arrays.asList("imggroup"));
	
	/**
	 * Underlying Glossary object, if any.
	 */
	protected Glossary glossary;
	
	/**
	 * A map of all glossary word variations to actual glossary words.
	 */
	protected Map<String, String> glossaryMap;
	
	/**
	 * A list of glossary words that have already been linked in this section.
	 */
	protected Set<String> usedEntryIds;
	
	/**
	 * Empty Constructor.  This is used by subclassses that do not use a
	 * {@link Glossary} object.  In such cases, the subclasses should override
	 * {@link #applyTransform(Element, TransformParameters)} to set the {@link #glossaryMap}
	 * before execution and {@link #getLastModified(TransformParameters)}. 
	 */
	protected GlossaryTransformer() {
		/* Empty */
	}

	/**
	 * Constructor.  If the {@link Glossary} parameter is null, this transformer
	 * will not take any action unless many methods are overridden in a subclass.
	 * 
	 * @param glossary
	 */
	public GlossaryTransformer(Glossary glossary) {
		this.glossary = glossary;
		this.glossaryMap = (glossary == null ? null : glossary.getMapTermToId());
	}
	
	@Override
	public Element applyTransform(Element elt, TransformParameters params) {
		if (glossaryMap != null) {
			usedEntryIds = new TreeSet<String>();
			glosswordsToLinks(elt);
		}
		return elt;
	}
	
	/**
	 * This method returns the last modified time of the
	 * underlying {@link Glossary} object.  Implementations
	 * that do not use a Glossary object must override
	 * this to provide an accurate time.
	 * 
	 */
	@Override
	public Time getLastModified(TransformParameters params) {
		if (glossary != null)
			return null;  // FIXME this no longer works:  glossary.getDocument().getLastModified();
		else
			return null;
	}
	
	/** 
	 * Recursively walk through page document and
	 * determine which text nodes are in paragraphs.
	 * Pass paragraph text nodes to routine that creates 
	 * glossary word links.
	 */
	protected void glosswordsToLinks(Node n) {
		
		try {
			NodeList list = n.getChildNodes();
			for(int i = 0; i < list.getLength(); i++) {
				Node child = list.item(i);
				if(child.getNodeType() == Node.TEXT_NODE 
						&& glosswordCheckElements.contains(child.getParentNode().getNodeName())) {
					replaceGlossWords(child.getParentNode(), (Text)child);
				} else if(child.getNodeType() == Node.ELEMENT_NODE) {
					Element childe = (Element)child;
					if (!glosswordBlockingElements.contains(childe.getLocalName())
						&& !glosswordBlockingClasses.contains(childe.getAttributeNS(null, "class"))) {
						glosswordsToLinks(childe);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/** 
	 * Search paragraph for glossary words.  Change first occurrence
	 * of group of words belonging to same entryId (main word and alternates)
	 * to link to glossary page.  After word is found, the word and all
	 * alternates are not longer linked later in the page.
	 */
	protected void replaceGlossWords(Node parent, Text origTextNode) {
		
		Document ownerDoc = parent.getOwnerDocument();
		DocumentFragment frag = ownerDoc.createDocumentFragment();
		String oriText = origTextNode.getNodeValue();
		Set<WordLocation> wordLocationsFirstOccurs = new TreeSet<WordLocation>(new WordLocComparator());
		HashMap<String, TreeSet<WordLocation>> entryIdWordLocations = 
			new HashMap<String, TreeSet<WordLocation>>();

		// Search for any and all glossary words in the node
		// Create a WordLocation object for each word.
		// Note: These WordLocations can overlap (e.g. "mature ecosystem" will match both "mature ecosystem" and "ecosystem")
		for(String glossword : glossaryMap.keySet()) {
			// Build a regex to match glossary word or phrase
			String regex = "\\b" + glossword.replace(" ", "\\s+") + "\\b"; 
			// log.debug("Checking {} with pattern {}", parent, regex);
			Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(oriText); // get a matcher object
			boolean found = m.find();
			if(found) {
				String entryId = glossaryMap.get(glossword);
				WordLocation wordLoc = new WordLocation(glossword, m.start(), m.end());
				if(entryIdWordLocations.containsKey(entryId)) {
					entryIdWordLocations.get(entryId).add(wordLoc);
				}
				else {  //create new set of word locations for entryId
					TreeSet<WordLocation> wordLocations = new TreeSet<WordLocation>(new WordLocComparator());
					wordLocations.add(wordLoc);
					entryIdWordLocations.put(entryId, wordLocations);
				}
			}
		}
		
		//get first entry from each word set, create new ordered set
		for(TreeSet<WordLocation> wordLocSet : entryIdWordLocations.values()) {
			WordLocation first = wordLocSet.first();
			wordLocationsFirstOccurs.add(new WordLocation(first.glossword, first.start, first.end));
		}
			
		// Process Segment - Word Locations are sorted in the order they are found in the text
		int unmarkedStartIndex = 0;  // Pointer through the fragment
		for(WordLocation loc: wordLocationsFirstOccurs) {
			
			// Only add a word after the previously added word, to avoid overlap
			if(unmarkedStartIndex > loc.start) continue;
			
			// Do not add a word link if it has already happened in the section.  Instead, just copy the text
			if (usedEntryIds.contains(glossaryMap.get(loc.glossword))) {
				frag.appendChild(ownerDoc.createTextNode
						(oriText.substring(unmarkedStartIndex, loc.end)));
				unmarkedStartIndex = loc.end;
				continue;
			}
			
			// Copy over the text up to the link
			frag.appendChild(ownerDoc.createTextNode
					(oriText.substring(unmarkedStartIndex, loc.start)));
			
			// Create the glossary link element
			Element linkElement = 
				createGlossaryElement(ownerDoc, glossaryMap.get(loc.glossword), oriText.substring(loc.start, loc.end));
			frag.appendChild(linkElement);
			
			// Check to see if this node should be recorded (and not repeated) or linked in a way where it can be linked again
			// This is useful if some glossary links occur outside of, but before, the main text and you wish to have them linked twice.
			boolean record = true;
			for(Node check = parent; check != null && check.getLocalName() != null && !check.getLocalName().equals("level1"); check = check.getParentNode()) {
				if (glosswordLinkButNotRecordElements.contains(check.getLocalName())) {
					record = false;
					break;
				}
			}
			if (record)
				usedEntryIds.add(glossaryMap.get(loc.glossword));
			
			// Set pointer to just after the detected glossary word
			unmarkedStartIndex= loc.end;
		}
		
		// Append last unmarked part
		frag.appendChild(ownerDoc.createTextNode(oriText.substring(unmarkedStartIndex)));
		
		parent.replaceChild(frag, origTextNode);
	}
	
	/** 
	 * Create and return a glossary link
	 * This will be an element whose local name is "gl" and whose namespace is the same as the 
	 * namespace of the top-level element in the document.
	 *  
	 * @param doc Document in which link is to be created
	 * @param entryId id of glossary element, used in attribute
	 * @param content text string content of element
	 * @return
	 */
	protected Element createGlossaryElement (Document doc, String entryId, String content) {
		Element linkElement = doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"gl");
		linkElement.setAttribute("entryId", entryId);
		linkElement.appendChild(doc.createTextNode(content));
		return linkElement;
	}
	
	/** 
	 * Stores beginning and ending character positions of a glossary word within a text block.
	 */
	protected class WordLocation{ 
		String glossword;
		int start;
		int end;
		int length;

		public WordLocation(String glossword, int start, int end) {
			this.glossword = glossword;
			this.start = start;
			this.end = end;
			this.length = end - start;
			
		}
	}
	
	protected class WordLocComparator implements Comparator<WordLocation> {
		@Override
		public int compare(WordLocation o1, WordLocation o2) {
			// If the locations have the same start point, they overlap - choose the longer word first
			if (((Integer)o1.start).equals(o2.start)) {
				return o2.length - o1.length;
			} else {
				return ((Integer)(o1.start)).compareTo((o2.start));
			}
		}
	}
	
	public GlossaryTransformer addGlosswordCheckElement(String e) {
		glosswordCheckElements.add(e);
		return this;
	}
	
	public GlossaryTransformer addGlosswordBlockingElement(String e) {
		glosswordBlockingElements.add(e);
		return this;
	}
	
	public GlossaryTransformer addGlosswordBlockingClass(String e) {
		glosswordBlockingClasses.add(e);
		return this;
	}
	
	public GlossaryTransformer glosswordLinkButNotRecordElements(String e) {
		glosswordLinkButNotRecordElements.add(e);
		return this;
	}

}
