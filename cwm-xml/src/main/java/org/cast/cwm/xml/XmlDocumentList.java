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
import java.util.Iterator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * An ordered list of XmlDocument objects.
 * This class supports some convenient operations over a whole set of XML documents,
 * such as locating pages (or in general, Label counts) that are numbered sequentially
 * over several files.
 *  
 * @author borisgoldowsky
 *
 */
public class XmlDocumentList implements Iterable<XmlDocument> {
	
	@Getter @Setter protected List<XmlDocument> documentList;
	
	public XmlDocumentList () {
		documentList = new ArrayList<XmlDocument>();
	}
	
	public XmlDocumentList add (XmlDocument... documents) {
		for (XmlDocument doc : documents)
			documentList.add(doc);
		return this;
	}
	
	/**
	 * Get the XmlSection for the Nth label from this ordered list of XmlDocuments.
	 * As with {@link XmlDocument#getByLabel(Serializable, Integer)}, 
	 * the count starts from 1 and goes in document order.
	 * In this case, it continues on to each document in the list until the given N is reached.
	 * @param label which label category to use
	 * @param num (N) the index to retrieve
	 * @return the XmlSection, or null.
	 */
	public XmlSection getByLabel(Serializable label, int num) {
		for (XmlDocument doc : documentList) {
			if (num > doc.getLabelCount(label))
				num -= doc.getLabelCount(label);
			else
				return doc.getByLabel(label, num);
		}
		return null;
	}
	
	/**
	 * For an XmlSection with a given label, return the position of the XmlSection
	 * among those in the XmlDocumentList with that same label.
	 * 
	 * @param label
	 * @param sec the XmlSection
	 * @return the (1-based) position of the XmlSection, or -1 if not found.
	 */
	public int getLabelIndex(Serializable label, XmlSection sec) {
		int count = sec.getXmlDocument().getLabelIndex(label, sec);
		if (count == -1)
			return -1;  // sec wasn't found at all, or doesn't have the given label.
		// Add to count the number of labels in each preceding document.
		for (XmlDocument doc : documentList) {
			if (doc == sec.getXmlDocument())
				break;
			count += doc.getLabelCount(label);
		}
		return count;
	}
	
	/**
	 * Return the total number of labels of the given type in all the documents of this XmlDocumentList.
	 * @param label
	 * @return total count.
	 */
	public int getLabelCount(Serializable label) {
		int count = 0;
		for (XmlDocument doc : documentList)
			count += doc.getLabelCount(label);
		return count;
	}
	
	@Override
	public Iterator<XmlDocument> iterator() {
		return documentList.iterator();
	}

}
