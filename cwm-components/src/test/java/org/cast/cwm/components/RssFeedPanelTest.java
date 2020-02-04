/*
 * Copyright 2011-2020 CAST, Inc.
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

import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class RssFeedPanelTest {

	private WicketTester tester;

	@Before
	public void setUp() {
		tester = new WicketTester();
	}
	
	@Test
	public void canRender() {
		// This is probably not best practice since it goes out the internet to retrieve URL as part of test,
		// but is there a better way?
		tester.startComponentInPage(new RssFeedPanel("panel", "http://rss.slashdot.org/Slashdot/slashdot", 3));
		tester.assertComponent("panel", RssFeedPanel.class);
		// Should have 3 links in the panel
		tester.assertComponent("panel:item:0:link", ExternalLink.class);
		tester.assertComponent("panel:item:1:link", ExternalLink.class);
		tester.assertComponent("panel:item:2:link", ExternalLink.class);
	}
	
}
