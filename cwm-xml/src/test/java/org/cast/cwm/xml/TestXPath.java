/*
 * Copyright 2011-2016 CAST, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.cast.cwm.test.CwmBaseTestCase;
import org.cast.cwm.test.InjectionTestHelper;
import org.cast.cwm.xml.service.IXmlService;
import org.cast.cwm.xml.service.XmlService;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.cwm.xml.transform.TransformParameters;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestXPath extends CwmBaseTestCase<InjectionTestHelper> {
	
	private static final XPathFactory factory = XPathFactory.newInstance();
	
	private Element dom;
	private XPath xPath;
	
	private static final byte[] doc = ("<level1 xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\">"
		+ "<level2><p>First p</p><p><em>Second p</em></p></level2>"
		+ "<level2><p>Third p</p></level2>"
		+ "<p>Fourth p</p></level1>")
		.getBytes();
	
	@Override
	protected boolean isApplicationThemed() {
		return false;
	}


	@Override
	protected InjectionTestHelper getInjectionTestHelper() {
		return new InjectionTestHelper(); 
	}

	@Override
	public void setUpData() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document document;
		DocumentBuilder db = dbf.newDocumentBuilder();
		document = db.parse(new ByteArrayInputStream(doc));
		dom = document.getDocumentElement();

		xPath = factory.newXPath();
		xPath.setNamespaceContext(new XmlService().getNamespaceContext());
	}
	
	@Override
	public void populateInjection(InjectionTestHelper injectionHelper) throws Exception {
		IXmlService xmlService = mock(IXmlService.class);
		when(xmlService.getNamespaceContext()).thenReturn(new XmlService.CwmNamespaceContext());
		injectionHelper.injectObject(IXmlService.class, xmlService);
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
	public void testCountFollowing() throws XPathExpressionException {
		Element node = (Element) dom.getElementsByTagName("level2").item(0);

		Double n = (Double) xPath.evaluate("count(following::dtb:level2)", node, XPathConstants.NUMBER);
		assertEquals("Should be 1 level2 after the first level2", Double.valueOf(1.0), n);
		
		n = (Double) xPath.evaluate("count(following::dtb:p)", node, XPathConstants.NUMBER);
		assertEquals("Should be 2 <p> after the first level2", Double.valueOf(2.0), n);
		
		n = (Double) xPath.evaluate("count(following::dtb:p)", node.getChildNodes().item(0), XPathConstants.NUMBER);
		assertEquals("Should be 3 <p> after the first p inside the first level2", Double.valueOf(3.0), n);
		
		n = (Double) xPath.evaluate("count(following::dtb:em)", node, XPathConstants.NUMBER);
		assertEquals("Should be no <em> after the first level2", Double.valueOf(0.0), n);
	}
	
	@Test
	public void testCountPreceding() throws XPathExpressionException {
		Element node = (Element) dom.getElementsByTagName("level2").item(1);

		Double n = (Double) xPath.evaluate("count(preceding::dtb:level2)", node, XPathConstants.NUMBER);
		assertEquals("Should be 1 level2 before the second level2", Double.valueOf(1.0), n);
		
		n = (Double) xPath.evaluate("count(preceding::dtb:p)", node, XPathConstants.NUMBER);
		assertEquals("Should be 2 <p> before the second level2", Double.valueOf(2.0), n);
		
		n = (Double) xPath.evaluate("count(preceding::dtb:p)", node.getChildNodes().item(0), XPathConstants.NUMBER);
		assertEquals("Should be 2 <p> before the p inside the second level2", Double.valueOf(2.0), n);		

		n = (Double) xPath.evaluate("count(preceding::dtb:em)", node, XPathConstants.NUMBER);
		assertEquals("Should be 1 <em> before the second level2", Double.valueOf(1.0), n);
	}
	
	@Test
	public void testFilterElements() {
		// Use first <level2> as the context node
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
