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
		assertEquals("Content disposition header is wrong", "attachment; filename=\"fakefile\"", tester.getContentDispositionFromResponseHeader());
		tester.assertContains("testdata");
	}

}
