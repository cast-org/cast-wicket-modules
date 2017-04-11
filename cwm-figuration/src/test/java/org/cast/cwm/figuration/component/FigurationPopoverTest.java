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
package org.cast.cwm.figuration.component;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author bgoldowsky
 */
public class FigurationPopoverTest extends WicketTestCase {

	@Test
	public void canRender() {
		tester.startComponentInPage(new FigurationPopover<>("id")
				.setContent(new PopoverBodyPanel("content")));
		tester.assertComponent("id", FigurationPopover.class);
		tester.assertComponent("id:content", PopoverBodyPanel.class);
		tester.assertContains("Test popover body content.");
	}

	@Test
	public void snapshotTest() throws Exception {
		tester.startComponentInPage(new FigurationPopover<>("id")
				.setContent(new PopoverBodyPanel("content")));
		tester.assertResultPage(getClass(),"snapshot/FigurationPopoverTest.html");
	}

	private class PopoverBodyPanel extends Panel {

		public PopoverBodyPanel(String id) {
			super(id);
		}
	}

}