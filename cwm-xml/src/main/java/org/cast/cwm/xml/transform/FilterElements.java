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
package org.cast.cwm.xml.transform;

import com.google.inject.Inject;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.xml.service.IXmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.Serializable;

/**
 * <p>
 * Reduces a set of Elements based on an XPath Expression via values
 * set in the {@link TransformParameters} object.
 * </p>
 * <p>
 * Example:
 * <pre>
 *   TransformParameters params = new TransformParameters();
 *   params.set(FilterElements.XPATH, "//dtb:responsegroup"); // Only include response groups
 * </pre>
 * @author jbrookover
 *
 */
public class FilterElements implements IDOMTransformer, Serializable {

	private static final long serialVersionUID = 1L;
	public static final String XPATH = "FilterElements:xpath";
	
	private static final XPathFactory factory = XPathFactory.newInstance();
	
	@Inject
	IXmlService xmlService;
	
	private static final Logger log = LoggerFactory.getLogger(FilterElements.class);

	public FilterElements() {
		Injector.get().inject(this);
	}
	
	@Override
	public Element applyTransform(Element n, TransformParameters params) {
		
		// Check to see if we need to apply the transform
		if (params == null || !params.containsKey(XPATH))
			return n;

		try {
			XPath xPath;
			synchronized(factory) {  // XPathFactory is not thread-safe
				xPath = factory.newXPath();
			}
			xPath.setNamespaceContext(xmlService.getNamespaceContext());
			NodeList keep = (NodeList) xPath.evaluate((String) params.get(XPATH), n, XPathConstants.NODESET);

			log.trace("FilterElements using {} found {} nodes", params.get(XPATH), keep.getLength());
			
			Element target = null;

			// If there are multiple nodes returned by the XPath, 
			// then remove all children of the root node and then
			// add the matched nodes as new children.
			//
			// Otherwise, just return the single node matched by
			// the XPath.
			// 
			// TODO: Options for different behavior?  Creating a new <DIV> ?
			if (keep.getLength() > 1) {

				target = n; 
				
				Node remove = target.getFirstChild();
				while (remove != null) {
					target.removeChild(remove);
					remove = target.getFirstChild();
				}
								
				for (int i = 0; i < keep.getLength(); i++) {
					target.appendChild(keep.item(i));
				}
			} else {
				target = (Element) keep.item(0);
			}
			
			return target;
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Time getLastModified(TransformParameters params) {
		return null;  // this transformation will not change over time.
	}

}
