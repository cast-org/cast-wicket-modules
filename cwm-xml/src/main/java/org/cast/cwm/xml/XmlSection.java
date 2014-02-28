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
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.cast.cwm.xml.parser.XmlParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a section of an XML document.  A "section" in this case means a structural element,
 * such as a chapter or page.
 * 
 * @author bgoldowsky
 *
 */
public class XmlSection implements IXmlPointer, Serializable, Comparable<XmlSection> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The ID of the root XmlSection.
	 */
	public static final String DOCUMENT_ID = "docID";
	
	@Getter protected XmlDocument xmlDocument;
	@Getter Element element;
	@Getter @Setter protected String id;
	@Getter @Setter protected String title;
	@Getter @Setter protected String subTitle;
	@Getter @Setter protected String className;
	@Getter protected XmlSection parent;
	@Getter protected List<XmlSection> children = new ArrayList<XmlSection>();
	@Getter @Setter
	protected int sortOrder = -1; // The order of this section within a document.

	/**
	 * Set up an XmlSection with the basic values.
	 * 
	 * @param document the parent XmlDocument, not to be confused with {@link Document}
	 * @param parent the parent XmlSection, or null if this is the root
	 * @param id the Id of this section (root Id is {@link #DOCUMENT_ID})
	 * @param elt the DOM element of this section
	 * @param title the title of this section ({@link XmlParser} implementation determines what this value is)
	 */
	public void init (XmlDocument document, XmlSection parent, String id, Element elt, String title) {
		this.xmlDocument = document;
		this.parent = parent;
		this.id = id;
		this.element = elt;
		this.title = title;
	}
	
	public XmlSection addChild (String id, Element elt, String title) {
		XmlSection child;
		try {
			child = this.getClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Could not instantiate " + this.getClassName());
		}
		child.init(xmlDocument, this, id, elt, title);
		if (children == null)
			children = new ArrayList<XmlSection>();
		children.add(child);
		return child;
	}
	
	public int getIndex () {
		if (parent != null)
			return parent.children.indexOf(this);
		else
			return 0;
	}
	
	/** Return the Nth child of this section, or null if none */
	public XmlSection getChild(int n) {
		if (children != null && n >= 0 && n < children.size())
			return children.get(n);
		else
			return null;
	}
	
	public boolean hasChildren() {
		return (children != null && !children.isEmpty());
	}
	
	/** Return the section's next sibling section, if any, or null. */
	public XmlSection getNext() {
		if (parent != null)
			return parent.getChild(getIndex()+1);
		else
			return null;
	}
	
	/** Return the section's preceding sibling section, if any, or null. */
	public XmlSection getPrev() {
		if (parent != null)
			return parent.getChild(getIndex()-1);
		else
			return null;
	}
	
	/** Return the following section: if no next sibling, then the parent's
	 * next sibling, and so forth.
	 * @return
	 */
	public XmlSection getFollowing() {
		XmlSection sibling = getNext();
		if (sibling != null)
			return sibling;
		if (getParent() == null)
			return null;
		return getParent().getFollowing();
	}
	
	/** Return the preceding section: if no prev. sibling, then the parent's
	 * prev. sibling, and so forth.
	 * @return
	 */
	public XmlSection getPreceding() {
		XmlSection sibling = getPrev();
		if (sibling != null)
			return sibling;
		if (getParent() == null)
			return null;
		return getParent().getPreceding();
	}

	/**
	 * Traverse the tree forwards from this node to the next node
	 * that has the given element type.
	 * @param type
	 * @return a matching XmlSection or null
	 */
	public XmlSection getFollowingOfType(String type) {
		return traverseForwardForType(type, false);
	}
	
	/**
	 * Traverse the tree backwards from this node to the previous node
	 * that has the given element type.
	 * @param type
	 * @return a matching XmlSection or null
	 */
	public XmlSection getPrecedingOfType(String type) {
		return traverseBackwardForType(type, false);
	}
	
	/** Return the next section that has the same element type as the 
	 * current section.
	 * @return a matching XmlSection or null
	 */
	public XmlSection getFollowingSameType() {
		return getFollowingOfType(getType());
	}

	/** Return the previous section that has the same element type as the 
	 * current section.
	 * @return a matching XmlSection or null
	 */
	public XmlSection getPrecedingSameType() {
		return getPrecedingOfType(getType());
	}


	protected XmlSection traverseForwardForType(String type, boolean considerCurrent) {
		// 1. Try this node itself
		if (considerCurrent && type.equals(getType()))
			return this;
		
		// 2. Try children
		if (hasChildren())
			return getChild(0).traverseForwardForType(type, true);

		// 3. Try following sibling (or sibling of parent)
		XmlSection next = getFollowing();
		if (next != null)
			return next.traverseForwardForType(type, true);

		// 4. Give up
		return null;
	}	

	protected XmlSection traverseBackwardForType(String type, boolean considerCurrent) {
		// 1. Try this node itself
		if (considerCurrent && type.equals(getType()))
			return this;

		// 2. Try children
		if (hasChildren())
			return getChild(children.size()-1).traverseBackwardForType(type, true);

		// 3. Try preceding sibling, preceding sibling of parent, etc.
		XmlSection next = getPreceding();
		if (next != null)
			return next.traverseBackwardForType(type, true);

		// 4. Give up
		return null;
	}	

	public XmlSection getAncestor(String elementName) {
		if (element == null)
			return null;
		if (element.getLocalName().equals(elementName))
			return this;
		if (parent == null)
			return null;
		return parent.getAncestor(elementName);
	}
	
	/**
	 * Return List of ancestor XmlSectionModel items from top of TOC to the given element
	 * @param stripStart number of elements to remove from start of list (1 removes the TOC root, etc)
	 * @param stripEnd number of elements to remove from end of list (1 removes "this", etc)
	 * @return
	 */
	public List<XmlSectionModel> getBreadcrumbs(int stripStart, int stripEnd) {
		List<XmlSectionModel> list = new ArrayList<XmlSectionModel>();
		for (XmlSection s = this; s != null; s = s.parent)
			list.add(0, new XmlSectionModel(s));
		// Strip items from start and end of list
		// Constructs new ArrayList since the subList view is not Serializable
		return new ArrayList<XmlSectionModel>(list.subList(stripStart, list.size()-stripEnd));
	}
	
	/**
	 * Return a List of model objects for this section's sibling sections.
	 * Useful in page-by-page navigation.
	 */
	public List<XmlSectionModel> getSiblings() {
		List<XmlSectionModel> list = new ArrayList<XmlSectionModel>();
		for (XmlSection s : parent.children)
			list.add(new XmlSectionModel(s));
		return list;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		sb.append(id);
		sb.append(":");
		sb.append(title);
		sb.append(" ");
		if (children != null) {
			for (XmlSection c : children) {
				sb.append(c);
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}
	
	/** 
	 * Check whether this section encloses another section.
	 * @param sec XmlSection that may be a descendant
	 * @return true if given section is a descendant of this section.
	 */
	public boolean isAncestorOf(XmlSection sec) {
		do {
			sec = sec.parent;
			if (this.equals(sec)) {
				return true;
			}
		} while (sec != null);
		return false;
	}
	
	public String getNumbering(int depth, String separator) {
		if (depth > 1 && parent != null) {
			return parent.getNumbering(depth-1, separator) + separator + String.valueOf(getIndex()+1);
		} else {
			return String.valueOf(getIndex()+1);
		}
	}
	
	public String getType() {
		return element.getLocalName();
	}
	
	
	/**
	 * Returns the label used to describe this section.  By default, this returns an empty list.  Applications
	 * may override to apply a description to a section (e.g. "Page" or "Chapter").  
	 * 
	 * @return
	 * @see XmlDocument#getByLabel(Serializable, Integer)
	 */
	public List<? extends Serializable> getLabels() {
		return new ArrayList<Serializable>();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof XmlSection))
			return false;
		XmlSection osection = (XmlSection) other;
		return (this.getId().equals(osection.getId())
				&& this.getXmlDocument().equals(osection.getXmlDocument()));
	}

	@Override
	public int compareTo(XmlSection other) {
		if (this.getXmlDocument().equals(other.getXmlDocument())) {
			return this.getSortOrder() - other.getSortOrder();
		} else {
			return this.getXmlDocument().compareTo(other.getXmlDocument());
		}
	}

}
