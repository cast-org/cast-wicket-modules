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
package org.cast.cwm.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class DelimitedRepeatingViewTest {

	private WicketTester tester;

	@Before
	public void setUp() {
		tester = new WicketTester();
	}
	
	@Test
	public void canRender() {
		tester.startComponentInPage(makeView());
		tester.assertComponent("view", DelimitedRepeatingView.class);
	}
	
	@Test
	public void showsAllItems() {
		tester.startComponentInPage(makeView());
		tester.assertContains("first item");
		tester.assertContains("second item");
		tester.assertContains("third item");
	}
	
	@Test
	public void includesDefaultDelimiter() {
		tester.startComponentInPage(makeView());
		tester.assertContains("first item</span> <span");		
	}
	
	@Test
	public void includesSettableDelimiter() {
		DelimitedRepeatingView view = makeView();
		view.setDelimiter("DeLimit");
		tester.startComponentInPage(view);
		tester.assertContains("first item</span>DeLimit<span");
	}

	private DelimitedRepeatingView makeView() {
		DelimitedRepeatingView view = new DelimitedRepeatingView("view");
		view.add(new Label(view.newChildId(), "first item"));
		view.add(new Label(view.newChildId(), "second item"));
		view.add(new Label(view.newChildId(), "third item"));
		return view;
	}
	
}
