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

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.cast.cwm.xml.service.XmlService;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.cwm.xml.transform.TransformParameters;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestXPath extends TestCase {
	
	private static final XPathFactory factory = XPathFactory.newInstance();
	
	private Element dom;
	private XPath xPath;
	
	private static final byte[] doc = ("<level1 xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\">"
		+ "<level2><p>First p</p><p><em>Second p</em></p></level2><level2><p>Third p</p></level2><p>Fourth p</p></level1>").getBytes();
	
	@Override
	public void setUp() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document document;
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(new ByteArrayInputStream(doc));
			dom = document.getDocumentElement();
		} catch (Exception e) {
			fail();
		}

		xPath = factory.newXPath();
		xPath.setNamespaceContext(XmlService.get().getNamespaceContext());
	}
	
	
	@Test
	public void testSimpleXPath() throws XPathExpressionException {
		NodeList ns = (NodeList) xPath.evaluate("/dtb:level1", dom, XPathConstants.NODESET);
		assertEquals(1, ns.getLength());
		
		ns = (NodeList) xPath.evaluate("//dtb:level2", dom, XPathConstants.NODESET);
		assertEquals(2, ns.getLength());

		ns = (NodeList) xPath.evaluate("//dtb:p", dom, XPathConstants.NODESET);
		assertEquals(4, ns.getLength());
	}
	
	@Test
	public void testChildXPath() throws XPathExpressionException {
		NodeList ns = (NodeList) xPath.evaluate("//dtb:level2/dtb:p", dom, XPathConstants.NODESET);
		assertEquals(3, ns.getLength());

		ns = (NodeList) xPath.evaluate("//dtb:level1/dtb:level2/dtb:p", dom, XPathConstants.NODESET);
		assertEquals(3, ns.getLength());

		ns = (NodeList) xPath.evaluate("//dtb:level1/dtb:p", dom, XPathConstants.NODESET);
		assertEquals(1, ns.getLength());
	}
	
	@Test
	public void testFromNestedContext() throws XPathExpressionException {
		// Start these searches from the first level2 (which has 2 p elements inside it).
		Element node = (Element) dom.getElementsByTagName("level2").item(0);
		
		NodeList ns = (NodeList) xPath.evaluate("dtb:p", node, XPathConstants.NODESET);
		assertEquals(2, ns.getLength());
				
		ns = (NodeList) xPath.evaluate("dtb:p", node, XPathConstants.NODESET);
		assertEquals(2, ns.getLength());
		
		ns = (NodeList) xPath.evaluate("dtb:p/dtb:em", node, XPathConstants.NODESET);
		assertEquals(1, ns.getLength());
		assertEquals("em", ns.item(0).getLocalName());

		ns = (NodeList) xPath.evaluate("//dtb:p/dtb:em", node, XPathConstants.NODESET);
		assertEquals(1, ns.getLength());
	}
	
	@Test
	public void testFilterElements() {
		Element node = (Element) dom.getElementsByTagName("level2").item(0);
		TransformParameters params = new TransformParameters();
		
		params.put(FilterElements.XPATH, ".//dtb:p");
		Element output = new FilterElements().applyTransform((Element) node.cloneNode(true), params);
		assertEquals("First pSecond p", output.getTextContent());
		
		params.put(FilterElements.XPATH, ".//dtb:p/dtb:em");
		output = new FilterElements().applyTransform((Element) node.cloneNode(true), params);
		assertEquals("Second p", output.getTextContent());
		
		// The following fails -- I would have thought it would be ok
		//params.put(FilterElements.XPATH, "//dtb:p/dtb:em");
		//output = new FilterElements().applyTransform((Element) node.cloneNode(true), params);
		//assertEquals("Second p", output.getTextContent());
	}
	

}
