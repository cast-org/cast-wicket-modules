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
package org.cast.cwm.components;

import static org.junit.Assert.assertEquals;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class FileDownloadLinkTest {

	private WicketTester tester;
	
	private byte[] fakeFile = "testdata".getBytes();

	@Before
	public void setUp() {
		tester = new WicketTester();
	}
	
	@Test
	public void canRender() {
		tester.startComponentInPage(new FileDownloadLink("link", Model.of(fakeFile), "text/plain", "fakefile"));
		tester.assertComponent("link", FileDownloadLink.class);
		tester.assertEnabled("link");
	}
	
	@Test
	public void canDownload() {
		tester.startComponentInPage(new FileDownloadLink("link", Model.of(fakeFile), "text/plain", "fakefile"));
		tester.clickLink("link");
		assertEquals("Content length header is wrong", fakeFile.length, tester.getContentLengthFromResponseHeader());
		assertEquals("Content type header is wrong", "text/plain", tester.getContentTypeFromResponseHeader());
		assertEquals("Content disposition header is wrong", "attachment; filename=\"fakefile\"; filename*=UTF-8''fakefile", tester.getContentDispositionFromResponseHeader());
		tester.assertContains("testdata");
	}

}
