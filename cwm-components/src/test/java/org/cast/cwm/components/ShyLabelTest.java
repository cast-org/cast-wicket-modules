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

import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class ShyLabelTest {
	
	private WicketTester wicketTester;

	@Before
	public void setUp() {
		wicketTester = new WicketTester();
	}
	
	@Test
	public void canRenderWithAllContentShown() {
		wicketTester.startComponentInPage(new ShyLabel("id", new Model<String>("test")));
		wicketTester.assertComponent("id", ShyLabel.class);
		wicketTester.assertVisible("id");
	}
	
	@Test
	public void canRenderWithAllContentHidden() {
		wicketTester.startComponentInPage(new ShyLabel("id", new Model<String>("")));
		wicketTester.assertInvisible("id");
	}

	@Test
	public void canBeMadeInvisible() {
		ShyLabel label = new ShyLabel("id", new Model<String>("test"));
		wicketTester.startComponentInPage(label);
		wicketTester.assertComponent("id", ShyLabel.class);
		wicketTester.assertVisible("id");
		
		label.setDefaultModelObject("");
		label.render();
		wicketTester.assertInvisible("id");		
	}
	
	@Test
	public void canBeMadeVisible() {
		ShyLabel label = new ShyLabel("id", new Model<String>(""));
		wicketTester.startComponentInPage(label);
		wicketTester.assertInvisible("id");		

		label.setDefaultModelObject("test");
		label.render();
		
		wicketTester.assertComponent("id", ShyLabel.class);
		wicketTester.assertVisible("id");
	}
	
}
