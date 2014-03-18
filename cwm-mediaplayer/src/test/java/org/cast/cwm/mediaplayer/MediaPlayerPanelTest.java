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
