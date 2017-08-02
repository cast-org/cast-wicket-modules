/*
 * Copyright 2011-2017 CAST, Inc.
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
package org.cast.cwm.xml.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.Getter;

import org.apache.wicket.util.file.File;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.IInputStreamProvider;
import org.cast.cwm.xml.DomCache;
import org.cast.cwm.xml.FileXmlDocumentSource;
import org.cast.cwm.xml.ICacheableModel;
import org.cast.cwm.xml.IDocumentObserver;
import org.cast.cwm.xml.IXmlPointer;
import org.cast.cwm.xml.TransformResult;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.parser.XmlParser;
import org.cast.cwm.xml.transform.EnsureUniqueWicketIds;
import org.cast.cwm.xml.transform.IDOMTransformer;
import org.cast.cwm.xml.transform.TransformChain;
import org.cast.cwm.xml.transform.TransformParameters;
import org.cast.cwm.xml.transform.XslTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Service class to deal with XML Documents.
 * Has methods to load XML documents, set up and request XSL transforms, 
 * manage caching and updating from the filesystem, etc.
 *
 * Important: acts as a registry of files, so must be bound as a singleton instance.
 *  
 * @author bgoldowsky
 *
 */
public class XmlService implements IXmlService {
	
	/**
	 * Keeps track of all XmlDocuments in the system, hashed by their name.
	 * This allows XmlDocument objects to be accessed without serializing and keeping copies of them.
	 */
	private Map<String,XmlDocument> documents = new HashMap<String,XmlDocument>();
	
	/**
	 * Tracks all known transformers, by name.
	 */
	private Map<String,IDOMTransformer> transformers = new HashMap<String,IDOMTransformer>();
	
	/**
	 * Tracks all known transformer directories.  The order of these directories determines the 
	 * search order.  In general, load any custom directories first.
	 */
	private List<String> transformerDirectories = new ArrayList<String>();

	/**
	 * Wait at least this many seconds between checks of files for updates.
	 */
	@Getter
	private int updateCheckInterval = 10;
	
	/**
	 * Cache for DOMs generated by transforming XML.
	 */
	private DomCache domCache;

	@Getter
	private NamespaceContext namespaceContext = new CwmNamespaceContext();

	private static final Logger log = LoggerFactory.getLogger(XmlService.class);

	public XmlService () {
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#addTransformerDirectory(java.lang.String)
	 */
	@Override
	public void addTransformerDirectory(String dir) {
		transformerDirectories.add(dir);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#getDocument(java.lang.String)
	 */
	@Override
	public XmlDocument getDocument (String name) {
		return documents.get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#getTransformer(java.lang.String)
	 */
	@Override
	public IDOMTransformer getTransformer (String name) {
		return transformers.get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#newXmlSection()
	 */
	@Override
	public XmlSection newXmlSection() {
		return new XmlSection();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#findXslFile(java.lang.String)
	 */
	@Override
	public File findXslFile (String xslFileName) {
		File xslFile = new File(xslFileName);
		
		// Is filename absolute?
		if (xslFile.isAbsolute()) {
			if (xslFile.exists())
				return xslFile;
			log.error("XSL File not found: {}", xslFileName);
			return null;
		}

		// loop through the directories setup by the app to find the transformation file	
		for (String directory : transformerDirectories) {
			xslFile = new File(directory, xslFileName);
			if (xslFile.exists())
				return xslFile;
		}
		
		log.error("XSL file \"{}\" not found; looked in directories: {}", xslFileName, transformerDirectories);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#findXslResource(java.lang.String)
	 */
	@Override
	public FileXmlDocumentSource findXslResource (String xslFileName) {
		File file = findXslFile(xslFileName);
		if (file == null)
			throw new IllegalArgumentException("XSL file " + xslFileName + " not found.");
		return new FileXmlDocumentSource(file);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#loadXmlDocument(java.lang.String, org.apache.wicket.util.file.File, org.cast.cwm.xml.parser.XmlParser, java.util.List)
	 */
	@Override
	public XmlDocument loadXmlDocument (String name, File file, XmlParser parser, List<IDocumentObserver> observers) {
		return loadXmlDocument (name, new FileXmlDocumentSource(file), parser, observers);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#loadXmlDocument(java.lang.String, org.apache.wicket.Resource, org.cast.cwm.xml.parser.XmlParser, java.util.List)
	 */
	@Override
	public XmlDocument loadXmlDocument (String name, IInputStreamProvider xmlResource, XmlParser parser, List<IDocumentObserver> observers) {
		XmlDocument doc = new XmlDocument(name, xmlResource, parser, observers);
		doc.setSortOrder(documents.size());
		registerXmlDocument(name, doc);
		return doc;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#registerXmlDocument(java.lang.String, org.cast.cwm.xml.XmlDocument)
	 */
	@Override
	public void registerXmlDocument (String name, XmlDocument document) {
		if (documents.containsKey(name))
			throw new IllegalArgumentException("XML Document with duplicate name: " + name);
		documents.put(name, document);		
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#loadXSLTransformer(java.lang.String, org.apache.wicket.util.file.File, boolean, org.apache.wicket.util.file.File)
	 */
	@Override
	public IDOMTransformer loadXSLTransformer (String name, File xslFile, boolean forceUniqueWicketIds, File... dependentFiles) {
		IInputStreamProvider[] resources = new IInputStreamProvider[dependentFiles.length];
		for (int i=0; i<dependentFiles.length; i++)
			resources[i] = new FileXmlDocumentSource(dependentFiles[i]);
		return loadXSLTransformer(name, new FileXmlDocumentSource(xslFile), forceUniqueWicketIds, resources);
	}


	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#loadXSLTransformer(java.lang.String, java.lang.String, boolean, java.lang.String)
	 */
	@Override
	public IDOMTransformer loadXSLTransformer (String name, String xslFile, boolean forceUniqueWicketIds, String... dependentFiles) {
		IInputStreamProvider[] resources = new IInputStreamProvider[dependentFiles.length];
		for (int i=0; i<dependentFiles.length; i++)
			resources[i] = new FileXmlDocumentSource(new File(dependentFiles[i]));
		return loadXSLTransformer(name, new FileXmlDocumentSource(new File(xslFile)), forceUniqueWicketIds, resources);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#loadXSLTransformer(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public IDOMTransformer loadXSLTransformer (String name, String xslFileName, boolean forceUniqueWicketIds) {
		File xslFile = findXslFile(xslFileName);
		return loadXSLTransformer(name, new FileXmlDocumentSource(xslFile), forceUniqueWicketIds);
	}


	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#loadXSLTransformer(java.lang.String, org.apache.wicket.Resource, boolean, org.apache.wicket.Resource)
	 */
	@Override
	public IDOMTransformer loadXSLTransformer (String name, IInputStreamProvider xslResource, boolean forceUniqueWicketIds, IInputStreamProvider... dependentResources) {
		XslTransformer xsl = new XslTransformer(xslResource);
		for (IInputStreamProvider r : dependentResources)
			xsl.addDependentResources(r);
		IDOMTransformer transformer;
		if (forceUniqueWicketIds)
			transformer = new TransformChain(xsl, new EnsureUniqueWicketIds());
		else
			transformer = xsl;
		registerTransformer(name, transformer);
		return transformer;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#registerTransformer(java.lang.String, org.cast.cwm.xml.transform.IDOMTransformer)
	 */
	@Override
	public void registerTransformer (String name, IDOMTransformer transformer) {
		if (transformers.containsKey(name))
			throw new IllegalArgumentException("XML Document with duplicate name: " + name);
		transformers.put(name, transformer);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#getTransformed(org.cast.cwm.xml.ICacheableModel, java.lang.String)
	 */
	@Override
	public TransformResult getTransformed (ICacheableModel<? extends IXmlPointer> mXmlPtr, String transformName) {
		return getTransformed(mXmlPtr, transformName, null);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#getTransformed(org.cast.cwm.xml.ICacheableModel, java.lang.String, org.cast.cwm.xml.transform.TransformParameters)
	 */
	@Override
	public TransformResult getTransformed (ICacheableModel<? extends IXmlPointer> mXmlPtr, String transformName, TransformParameters params) {
		log.trace("Entering getTransformed({}, {})", mXmlPtr, transformName);
		IDOMTransformer transformer = getTransformer(transformName);
		// Getting the last modified time of these resources has the side effect of updating them if necessary.
		// Do that whether or not we have a cached DOM.
		Time xmlTime = mXmlPtr.getLastModified();
		Time transTime = transformer.getLastModified(params);
		// Now check the cache to see if it's as up to date as the XML and XSL files.
		net.sf.ehcache.Element cacheElement = getDomCache().get(mXmlPtr, transformName, params);
		if (cacheElement != null) {
			// Compare last-modified times of cache, XML, and transformer.
			Time cacheTime = Time.millis(cacheElement.getLastUpdateTime() != 0 ? cacheElement.getLastUpdateTime() : cacheElement.getCreationTime());
			// This call to getLastModified() will also update the XML if necessary
			if ((xmlTime==null || xmlTime.before(cacheTime)) && (transTime==null || transTime.before(cacheTime))) {
				// Cache is still valid
				log.trace("Returning cached DOM");
				return (TransformResult) cacheElement.getObjectValue();
			}
		}
		// Still here?  Cache was empty or outdated.  Do a new transform.
		log.trace("Cache {}, running transform", cacheElement==null ? "empty" : "outdated");
		IDOMTransformer trans = getTransformer(transformName);
		if (trans == null)
			throw new IllegalArgumentException("Transformer not registered: " + transformName);
		// Do the transform and cache the result.
		TransformResult tr;
		IXmlPointer xmlObj = mXmlPtr.getObject();
		if (xmlObj != null) {
			tr= new TransformResult(trans.applyTransform((Element) xmlObj.getElement().cloneNode(true), params));
		} else {
			// TODO: What if mXmlPtr is now pointing to null due to a change in the XML document?
			tr = null;
			log.warn("XmlPointer points to nothing: {}", mXmlPtr);
		}
		domCache.put(mXmlPtr, transformName, tr, params);
		return (tr);
	}

	protected DomCache getDomCache() {
		if (domCache == null)
			domCache = new DomCache();
		return domCache;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#addNamespaceContext(java.lang.String, java.lang.String)
	 */
	@Override
	public void addNamespaceContext(String prefix, String uri) {
		((CwmNamespaceContext) getNamespaceContext()).putNamespacePair(prefix, uri);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#getWicketNodes(org.w3c.dom.Element, boolean)
	 */
	@Override
	public NodeList getWicketNodes(Element elt, boolean all) {
		XPathFactory factory=XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		xPath.setNamespaceContext(getNamespaceContext());
		XPathExpression xp;
		NodeList nodes = null;
	
		try {
			if (all)
				xp = xPath.compile("//*[@wicket:id]");
			else
				xp = xPath.compile(".//*[@wicket:id][ancestor::*[@wicket:id][1] = current() or not(ancestor::*[@wicket:id])]");
			nodes = (NodeList) xp.evaluate(elt, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	
		return nodes;
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.xml.service.IXMLService#findTransformFile(java.lang.String)
	 */
	@Override
	public File findTransformFile(String href) {
		for (String directory : transformerDirectories) {
			File file = new File(directory, href);
			if (file.exists())
				return file;
		}
		return null;
	}

	public static class CwmNamespaceContext implements NamespaceContext, Serializable {

		private static final long serialVersionUID = 1L;

		private Map<String, String> prefix2uri = new HashMap<String, String>();
		private Map<String, String> uri2prefix = new HashMap<String, String>();
		
		public CwmNamespaceContext() {
			putNamespacePair("dtb", "http://www.daisy.org/z3986/2005/dtbook/");
			putNamespacePair("wicket", "http://wicket.apache.org");
			putNamespacePair(XMLConstants.DEFAULT_NS_PREFIX, "http://www.w3.org/1999/xhtml");
		}
		
		protected void putNamespacePair (String prefix, String uri) {
			prefix2uri.put(prefix, uri);
			uri2prefix.put(uri, prefix);
		}
		
		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix == null)
				throw new IllegalArgumentException("Prefix cannot be null");			
			return prefix2uri.get(prefix);
		}


		@Override
		public String getPrefix(String uri) {
			if (uri == null)
				throw new IllegalArgumentException("URI cannot be null");
			return uri2prefix.get(uri);
		}

		@Override
		public Iterator<Void> getPrefixes(String arg0) {
			log.error("Called unimplemented function getPrefixes");
			return null;
		}
	}

}
