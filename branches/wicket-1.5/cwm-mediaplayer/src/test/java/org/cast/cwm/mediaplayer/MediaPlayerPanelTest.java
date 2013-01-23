package org.cast.cwm.mediaplayer;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class MediaPlayerPanelTest {
	
	private WicketTester tester;

	@Before
	public void setUp()
	{
		tester = new WicketTester();
	}

	@Test
	public void canRender() {
		PackageResourceReference video = new PackageResourceReference(MediaPlayerPanelTest.class, "test.mov");
		tester.startComponentInPage(new MediaPlayerPanel("panel", video, 380, 240));
		tester.assertComponent("panel", MediaPlayerPanel.class);
		
		// TODO - what can we do to make sure the paths and such are correct?
		
	}
}
