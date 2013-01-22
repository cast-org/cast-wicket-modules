package org.cast.cwm.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class FileResourceTest {

	private WicketTester tester;

	@Before
	public void setUp() {
		tester = new WicketTester();
	}
	
	@Test
	public void canRender() throws ParseException {
		URL path = getClass().getClassLoader().getResource("1x1.gif");
		assertNotNull("Could not locate test image", path);
		tester.startResource(new FileResource(new File(path.getFile())));
		assertEquals("Wrong content type", "image/gif", tester.getContentTypeFromResponseHeader());
		assertEquals("Wrong content disposition", "inline", tester.getContentDispositionFromResponseHeader());
		// Should be sent with expiry header at least a day in the future
		Date expires = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'").parse(tester.getLastResponse().getHeader("Expires"));
		Date now = new Date();
		long oneDay = 24*60*60*1000;
		assertTrue("Too short, or no expiry header", expires.getTime() > now.getTime()+oneDay);
	}
	
}
