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
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author bgoldowsky
 */
public class FigurationModalBasicHeaderTest extends WicketTestCase {

	@Test
	public void canRender() {
		tester.startComponentInPage(new FigurationModalBasicHeader("id", Model.of("Test Heading")));
		tester.assertComponent("id", FigurationModalBasicHeader.class);
	}

	@Test
	public void showsHeader() {
		tester.startComponentInPage(new FigurationModalBasicHeader("id", Model.of("Test Heading")));
		tester.assertLabel("id:title", "Test Heading");
	}

	@Test
	public void showsCloseButton() {
		tester.startComponentInPage(new FigurationModalBasicHeader("id", Model.of("Test Heading")));
		tester.assertComponent("id:close", FigurationModalCloseButton.class);
	}

	@Test
	public void snapshotTest() throws Exception {
		tester.startComponentInPage(new FigurationModalBasicHeader("id", Model.of("Test Heading")));
		tester.assertResultPage(getClass(),"snapshot/FigurationModalBasicHeaderTest.html");
	}

}
