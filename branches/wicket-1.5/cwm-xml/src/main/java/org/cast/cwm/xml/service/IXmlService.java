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
package org.cast.cwm.xml.service;

import java.util.List;

import javax.xml.namespace.NamespaceContext;

import org.apache.wicket.util.file.File;
import org.cast.cwm.xml.FileResource;
import org.cast.cwm.xml.ICacheableModel;
import org.cast.cwm.xml.IDocumentObserver;
import org.cast.cwm.xml.IXmlPointer;
import org.cast.cwm.xml.TransformResult;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.parser.XmlParser;
import org.cast.cwm.xml.transform.IDOMTransformer;
import org.cast.cwm.xml.transform.TransformParameters;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Service interface to load XML documents, set up and request XSL transforms, manage caching and updating from the filesystem, etc.
 * 
 * Extracted from XMLService implementation.
 *  
 * @author Don Roby
 *
 */
public interface IXmlService {

	/*
	 * TODO: hide this implementation detail
	 */
	void setXmlSectionClass(Class<? extends XmlSection> clazz);

	/*
	 * TODO: hide this implementation detail
	 */
	void setUpdateCheckInterval(int interval);

	/*
	 * TODO: hide this implementation detail
	 */
	int getUpdateCheckInterval();

	/*
	 * TODO: hide this implementation detail
	 */
	NamespaceContext getNamespaceContext();

	/**
	 * Add a directory to the list of known transformer directories.
	 * The order of these directories determines the 
	 * search order.  In general, load any custom directories first.
	 * @param name
	 */
	void addTransformerDirectory(String dir);

	/**
	 * Fetch an XmlDocument by name from the ones registered with this object.
	 * @param name
	 * @return the XmlDocument, or null.
	 */
	XmlDocument getDocument(String name);

	/**
	 * Fetch a transformer by name from the ones registered with this object.
	 * @param name
	 * @return the transformer, or null.
	 */
	IDOMTransformer getTransformer(String name);

	/**
	 * Return new XmlSection, or a subclass as set in this object's xmlSectionClass.
	 * @return the newly instantiated object
	 */
	XmlSection newXmlSection();

	/**
	 * Find a file, either by absolute pathname or relative to one of the defined transfomerDirectories.
	 * 
	 * @param xslFileName
	 * @return a File, or null if not found.
	 */
	File findXslFile(String xslFileName);

	/**
	 * Find a file, either by absolute pathname or relative to one of the defined transfomerDirectories,
	 * and return as a FileResource. Will throw an error if file is not found.
	 * 
	 * @param xslFileName
	 * @return a FileResource, or null if not found.
	 */
	FileResource findXslResource(String xslFileName);

	/**
	 * Load an XML document from a File and register it by name with this object.  
	 * @see {@link #loadXmlDocument(String, Resource)}
	 * @param name name for this XML document (must be globally unique)
	 * @param file the file that contains the XML text
	 * @param parser the XML parser to be used
	 * @param observers (optional) list of initial document observers 
	 * @return the loaded and registered XmlDocument
	 */
	XmlDocument loadXmlDocument(String name, File file, XmlParser parser,
			List<IDocumentObserver> observers);

	/** 
	 * Read an XML document from the given Resource.
	 * It will be remembered by this service class and can thereafter be referred to by the given name.
	 *  
	 * @param name name for this XML document (must be globally unique)
	 * @param xmlResource Resource from which to read the XML content
	 * @param parser the XML parser to be used
	 * @param observers (optional) list of initial document observers
	 * @return the XmlDocument
	 */
	XmlDocument loadXmlDocument(String name, Resource xmlResource,
			XmlParser parser, List<IDocumentObserver> observers);

	/** 
	 * Register the provided XmlDocument object with this service class.
	 * @param name
	 * @param document
	 */
	void registerXmlDocument(String name, XmlDocument document);

	/**
	 * Create and load a transformer based on the given XSL File.
	 * If second argument is true, then a secondary transformation will be chained on, which makes
	 * sure that all wicket:id attributes (likely created by the XSL) are made unique. 
	 * @param name
	 * @param xslFile
	 * @param forceUniqueWicketIds
	 * @return the transformer
	 */
	IDOMTransformer loadXSLTransformer(String name, File xslFile,
			boolean forceUniqueWicketIds, File... dependentFiles);

	/**
	 * Create and load a transformer based on the given XSL File.
	 * If second argument is true, then a secondary transformation will be chained on, which makes
	 * sure that all wicket:id attributes (likely created by the XSL) are made unique. 
	 * @param name
	 * @param xslFile
	 * @param forceUniqueWicketIds
	 * @return the transformer
	 */
	IDOMTransformer loadXSLTransformer(String name, String xslFile,
			boolean forceUniqueWicketIds, String... dependentFiles);

	/**
	 * Create and load a transformer based on the given XSL File.
	 * If second argument is true, then a secondary transformation will be chained on, which makes
	 * sure that all wicket:id attributes (likely created by the XSL) are made unique. 
	 * @param name
	 * @param xslFileName - this is either a fully qualified file name or just the file name itself
	 * @param forceUniqueWicketIds
	 * @return the transformer
	 */
	IDOMTransformer loadXSLTransformer(String name, String xslFileName,
			boolean forceUniqueWicketIds);

	/**
	 * Create and load a transformer based on the given Resource, which should point to an XSL document.
	 * If second argument is true, then a secondary transformation will be chained on, which makes
	 * sure that all wicket:ids created by the XSL are made unique. 
	 * @param name
	 * @param xslResource
	 * @param forceUniqueWicketIds
	 * @return the transformer
	 */
	IDOMTransformer loadXSLTransformer(String name, Resource xslResource,
			boolean forceUniqueWicketIds, Resource... dependentResources);

	/**
	 * Register the provided DOM Transformer under the name provided.
	 */
	void registerTransformer(String name, IDOMTransformer transformer);

	/**
	 * Get transformed version of given XML, either from cache or by executing transform.
	 * @param sec
	 * @param transformName
	 * @return the transformed DOM Element.
	 */
	TransformResult getTransformed(
			ICacheableModel<? extends IXmlPointer> mXmlPtr, String transformName);

	/**
	 * Get transformed version of given XML, either from cache or by executing transform.  You
	 * can optionally specify parameters that are considered part of the cache key.
	 * 
	 * @param sec
	 * @param transformName
	 * @return the transformed DOM Element.
	 */
	TransformResult getTransformed(
			ICacheableModel<? extends IXmlPointer> mXmlPtr,
			String transformName, TransformParameters params);

	/**
	 * @param res
	 * @return
	 * 
	 * This will return a string of the elements children.  Can be used to get the html content from a child/children.
	 */
	String serialize(Element res);

	void addNamespaceContext(String prefix, String uri);

	/**
	 * Finds wicket nodes for a given element. This method either returns all of the 
	 * wicket nodes (up and down the DOM) or the first layer of wicket nodes immediately
	 * below the provided element.
	 * 
	 * @param elt the element to search
	 * @param all true, if searching the entire tree.  false, if just searching for the first wicket children.
	 * @return
	 */
	NodeList getWicketNodes(Element elt, boolean all);

	File findTransformFile(String href);

}