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
package org.cast.cwm.xml;

import java.io.Serializable;

import lombok.EqualsAndHashCode;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.xml.service.IXmlService;

import com.google.inject.Inject;

/**
 * A Model pointing to an XmlSection.
 * Must be used since XmlSection is not serializable, but XmlSection and DtbookDocument objects
 * are generated once and cached so the models can detach and re-attach efficiently.
 * 
 * This method implements equals() and hashCode() based on the document name and xml ID.
 * Since these are generally stable and uniquely identify a piece of XML content, this model
 * can be used as a cache key.
 * 
 * @author bgoldowsky
 *
 */
@EqualsAndHashCode(callSuper=false)
public class XmlSectionModel implements IModel<XmlSection>, ICacheableModel<XmlSection> {
	
	private String docName;
	private String sectionId;
	
	@Inject
	private IXmlService xmlService;
	
	private static final long serialVersionUID = 1L;

	public XmlSectionModel (XmlSection s) {
		super();
		Injector.get().inject(this);
		setObject(s);
	}

	public void setObject (XmlSection section) {
		if (section != null) {
			docName   = section.getXmlDocument().getName();
			sectionId = section.getId();
		}
	}

	public XmlSection getObject () {
		XmlDocument doc = getDocument();
		if (doc == null)
			return null;
		return doc.getById(sectionId);
	}

	public Time getLastModified() {
		return getDocument().getLastModified();
	}

	protected XmlDocument getDocument() {
		return xmlService.getDocument(docName);
	}

	public Serializable getKey() {
		return docName + "_" + sectionId;
	}
	
	public void detach() {		
	}
	
	public String toString() {
		return "XmlSectionModel[" + getKey() + "]";
	}
}
