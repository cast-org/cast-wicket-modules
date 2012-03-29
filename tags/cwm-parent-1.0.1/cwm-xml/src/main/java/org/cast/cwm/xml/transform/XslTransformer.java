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
package org.cast.cwm.xml.transform;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import lombok.Getter;

import org.apache.wicket.Resource;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.xml.service.XmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XslTransformer implements IDOMTransformer {
	
	@Getter protected Resource xslFile;
	private transient Templates xslTemplates;
	
	protected Time lastCheckedTime;
	private Time xslLastModified;

	/** 
	 * Files, other than the main XSL file, to check for modifications.
	 * These would typically be subsidiary XSL files that are imported by the main XSL.
	 */
	protected Set<Resource> dependentResources = new HashSet<Resource>();

	private static final Logger log = LoggerFactory.getLogger(XslTransformer.class);

	public XslTransformer (Resource xslFile) {
		this.xslFile = xslFile;
	}
	
	/**
	 * Declare the given resource as a dependency of this transformation.
	 * This would typically be subsidiary XSL files that are imported by the main XSL.
	 * Modification times of these dependent resources will be considered when the
	 * modification time of this transformer is requested.
	 * 
	 * @param resource
	 * @return this, for chaining
	 */
	public XslTransformer addDependentResources (Resource... resources) {
		for (Resource r : resources) {
			dependentResources.add(r);
		}
		return this;
	}
	
	/**
	 * Runs the transformation, returns the result.
	 */
	public Element applyTransform(Element element, TransformParameters params) {
		DOMResult res = new DOMResult();
		log.debug("Running XSLT {} on {}", xslFile, element.getAttributeNS(null, "id"));
		try {
			Source eSource = new DOMSource(element);
			Transformer transformer = getTransformer();
			// Pass in params as XSL parameters.  Null values are not sent; they can't be translated into XSL.
			if (params != null)
				for (Entry<String, Object> par : params.entrySet())
					if (par.getValue() != null)
						transformer.setParameter(par.getKey(), par.getValue());
			transformer.transform(eSource, res);
			return ((Document) res.getNode()).getDocumentElement();
		} catch (Exception e) {
			throw new RuntimeException("XSL transform failed", e);
		}
	}

	/**
	 * Returns the last modified time of the XSL file or any known dependent resources.
	 * (That is, any that have been been made known to this class via {@link #addDependentResource}.
	 * To avoid constantly reading the disk or DAV connection, this will only check as often as
	 * specified by XmlService's updateCheckInterval; otherwise returning a remembered value.
	 */
	public Time getLastModified(TransformParameters params) {
		if (lastCheckedTime == null || lastCheckedTime.elapsedSince().seconds() > XmlService.get().getUpdateCheckInterval())
			updateLastModified();
		return xslLastModified;
	}

	/**
	 * Check if disk file has been updated, and if so invalidate cached information in this object.
	 * To avoid constantly reading the disk, don't call this method directly; call {@link #getLastModified()} instead.
	 */
	synchronized protected void updateLastModified() {
		lastCheckedTime = Time.now();
		Time lastMod = xslFile.getResourceStream().lastModifiedTime();
		for (Resource r : dependentResources) {
			Time rtime = r.getResourceStream().lastModifiedTime();
			if (rtime.after(lastMod))
				lastMod = rtime;
		}
		if (xslLastModified==null || lastMod.after(xslLastModified)) {
			xslLastModified = lastMod;
			xslTemplates = null;  // invalidate cached templates
		}
	}
	
	public synchronized Templates getXslTemplates() 
	  throws TransformerConfigurationException, ResourceStreamNotFoundException, TransformerFactoryConfigurationError {
		if (xslTemplates == null)
			readXSL();
		return xslTemplates;
	}
	
	private Transformer getTransformer() 
	  throws TransformerConfigurationException, ResourceStreamNotFoundException, TransformerFactoryConfigurationError {
		return getXslTemplates().newTransformer();
	}

	/** 
	 * Load XSLT Templates from the given stylesheet file. 
	 * This is called when the DtbookDocument is created,
	 * any time that the XSLT file is changed, and whenever the
	 * DtbookDocument object is re-instantiated after serialization
	 * since Templates is not serializable and is marked transient.
	 * @throws ResourceStreamNotFoundException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws TransformerConfigurationException 
	 */
	synchronized private void readXSL() 
	  throws ResourceStreamNotFoundException, TransformerConfigurationException, TransformerFactoryConfigurationError {
		log.debug ("Reading XSL stylesheet {}", xslFile);
		// Setup a transformer
		IResourceStream resourceStream = xslFile.getResourceStream();
		Source xslSource = new StreamSource(resourceStream.getInputStream());
		// We want to tell the parser the real file name of the XSL resource, so that it can 
		// follow relative filenames to any other XSL that may be included or imported.
		// TODO is there a more general way to do this?
		if (resourceStream instanceof FileResourceStream)
			xslSource.setSystemId(((FileResourceStream)resourceStream).getFile().getAbsolutePath());
		xslTemplates = TransformerFactory.newInstance().newTemplates(xslSource);

		// TODO do we still want this?
//		if (observers != null)
//			for (IDocumentObserver obs : observers)
//				obs.xslUpdated(this);
	}
	
}
