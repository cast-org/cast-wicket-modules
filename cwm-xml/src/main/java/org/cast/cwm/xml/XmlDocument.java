/*
 * Copyright 2011-2018 CAST, Inc.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.IInputStreamProvider;
import org.cast.cwm.InputStreamNotFoundException;
import org.cast.cwm.xml.parser.XmlParser;
import org.cast.cwm.xml.service.IXmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.inject.Inject;

/**
 * A class that parses and holds the high level structure of an XML document.  Also,
 * maintains a static map of all parsed documents.
 * 
 * @author bgoldowsky
 *
 */
public class XmlDocument implements Serializable, Comparable<XmlDocument> {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(XmlDocument.class);
	
	/**
	 * A set of observers that are watching this document for changes.
	 */
	@Getter protected List<IDocumentObserver> observers; 
	
	@Getter @Setter protected String name;
	@Getter @Setter protected int sortOrder = -1; // The order of this document within the curriculum
	@Getter protected XmlSection tocSection;
	@Getter protected String documentNamespace;

	@Getter protected IInputStreamProvider xmlFile;
	protected XmlParser parser;
	protected Time lastModified;
	protected Time lastCheckedTime;
	
	protected Map<String,XmlSection> idMap;
	protected Map<String,XmlSection> longDescMap;
	protected Map<Serializable, List<XmlSection>> labelMap;
	
	@Inject
	private IXmlService xmlService;
	
	public XmlDocument(String name, IInputStreamProvider xmlFile, XmlParser parser, List<IDocumentObserver> observers) {
		super();
		Injector.get().inject(this);
		this.name = name;
		this.xmlFile = xmlFile;
		this.parser = parser;
		this.observers = observers;
		updateIfModified();
	}

	/**
	 * Read the XML document associated with this object and set up all associated sections, ID cache, etc.
	 */
	private void readXML()  {
		log.debug("Reading XML document for {}", name);
		idMap = new HashMap<String, XmlSection>();
		parser.setIdMap(idMap);
		parser.setDoc(this);
		try {
			this.tocSection = parser.parse(this.xmlFile.getInputStream());
		} catch (InputStreamNotFoundException e) {
			throw new RuntimeException(e);
		}
		// FIXME put LD processing back in?
		// this.longDescMap = parser.generateLongDescriptions(this.tocSection);
		this.documentNamespace = tocSection.getElement().getNamespaceURI();
		
		if (observers != null)
			for (IDocumentObserver obs : observers)
				obs.xmlUpdated(this);
		
		labelMap = new HashMap<Serializable, List<XmlSection>>();
		parseLabels(this.tocSection);
	}
	
	/**
	 * Adds sections to {@link #labelMap} based on {@link XmlSection#getLabels()}.
	 * 
	 * @param sec the root section that will be searched
	 */
	protected void parseLabels(XmlSection sec) {
		
		for(Serializable label : sec.getLabels()) {
			if (labelMap.get(label) == null)
				labelMap.put(label, new ArrayList<XmlSection>());
			labelMap.get(label).add(sec);
		}
		if (sec.getChildren() != null) {
			for (XmlSection child : sec.getChildren()) {
				parseLabels(child);
			}
		}
	}

	/**
	 * Check and return the last modified time, updating the in-memory data if file has been modified.
	 * To avoid constantly reading the disk, this will only actually check based on XmlService's updateCheckInterval,
	 * otherwise returning the remembered value.
	 */
	public Time getLastModified() {
        doUpdateCheck();
		return lastModified;
	}

    /**
     * Do a routine check to see if document needs updating.
     * This method can be called frequently; it will do nothing if the update-check interval
     * has not elapsed since the last check.  If we haven't checked recently, then the
     * underlying file will be checked to see if it has changed, and if so, this XmlDocument object
     * will be updated.
     *
     * @return true if an update was actually made.
     */
    public boolean doUpdateCheck() {
        // Only actually look at the disk to find last-modified time every 10 seconds or so (whatever value is set in XmlService)
        if (lastCheckedTime == null || lastCheckedTime.elapsedSince().seconds() > xmlService.getUpdateCheckInterval())
            return updateIfModified();
        return false;
    }

	/**
	 * Check the disk (or DAV server, etc) to see if underlying file has been modified.
	 * If so, update the XMLDocument.
     * @return true if the document was updated.
	 */
	synchronized protected boolean updateIfModified() {
		log.trace("checking last modified time of {}", this);
		lastCheckedTime = Time.now();
		Time newLM = xmlFile.lastModifiedTime();
		if (lastModified==null || newLM.after(lastModified)) {
			lastModified = newLM;
			try {
				readXML();
                return true;
			} catch (Exception e) {
				// Log error, but allow program to go ahead with previously cached doc.
				e.printStackTrace();
			}
		}
        return false;
	}
	
	/**
	 * Get an XmlSection from this book by its id.
	 * 
	 * @param id
	 * @return
	 */
	public XmlSection getById(String id) {
		return idMap.get(id);
	}
	
	/**
	 * Get an XmlSection from this book by its label and (1-based) count within the book.  Applications
	 * must override {@link XmlSection#getLabels()} for this to function properly.
	 * 
	 * @param label a label as defined by {@link XmlSection#getLabels()}
	 * @param num specifying the nth instance of this labeled section in the book
	 * @return the matching XmlSection, or null if not found.
	 */
	public XmlSection getByLabel(Serializable label, Integer num) {
		if (labelMap == null || num < 1)
			return null;
		List<XmlSection> labels = labelMap.get(label);
		if (labels == null || num > labels.size())
			return null;
		return labelMap.get(label).get(num-1);
	}
	
	/**
	 * Return the total number of elements with the given label.
	 * @param label
	 * @return number of elements; 0 if no elements have that label.
	 */
	public int getLabelCount(Serializable label) {
		if (labelMap == null || labelMap.get(label) == null)
			return 0;
		else
			return labelMap.get(label).size();
	}
	
	/**
	 * Given an XmlSection with a particular label, return its position within the list
	 * of all elements with that label (1-based).  In other words, returns the number of 
	 * similarly-labeled elements including the section and those that precede it.
	 * @param label
	 * @param sec
	 * @return the index, or -1 if the element was not found or does not itself have the given label.
	 */
	public int getLabelIndex(Serializable label, XmlSection sec) {
		if (labelMap == null || labelMap.get(label) == null || !labelMap.get(label).contains(sec))
			return -1;
		int position = labelMap.get(label).indexOf(sec);
		return (position==-1) ? -1 : position+1;
	}
	
	public Document getDocument() {
		return tocSection.getElement().getOwnerDocument();
	}
	
	public XmlSection getLongDescSection (String imageId) {
		return longDescMap.get(imageId);
	}
	
	public XmlDocument addObserver(IDocumentObserver observer, boolean callImmediately) {
		if (observers == null)
			 observers = new ArrayList<IDocumentObserver>();
		this.observers.add(observer);
		if (callImmediately)
			observer.xmlUpdated(this);
		return this;
	}
	
	public XmlDocument setObservers(Iterable<IDocumentObserver> observers, boolean callImmediately) {
		for (IDocumentObserver obs : observers)
			addObserver(obs, callImmediately);
		return this;
	}

	@Override
	public int compareTo(XmlDocument other) {
		return this.getSortOrder() - other.getSortOrder();
	}

}
