/*
 * Copyright 2011 CAST, Inc.
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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.ITestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class ShyContainerTest {

	private WicketTester wicketTester;

	@Before
	public void setUp() {
		wicketTester = new WicketTester();
	}
	
	@Test
	public void canRenderWithAllContentShown() {
		wicketTester.startPanel(new VisibilityBasedTestPanelSource(true, true));
		wicketTester.assertComponent("panel:shy", ShyContainer.class);
		wicketTester.assertVisible("panel:shy");
	}

	@Test
	public void hiddenWithAllContentHidden() {
		wicketTester.startPanel(new VisibilityBasedTestPanelSource(false, false));
		wicketTester.assertInvisible("panel:shy");
	}

	@Test
	public void visibleWithFirstContentShown() {
		wicketTester.startPanel(new VisibilityBasedTestPanelSource(true, false));
		wicketTester.assertVisible("panel:shy");
	}

	@Test
	public void visibleWithSecondContentShown() {
		wicketTester.startPanel(new VisibilityBasedTestPanelSource(false, true));
		wicketTester.assertVisible("panel:shy");
	}

	@Test
	public void hiddenWithAllContentVisibilityDisallowed() {
		wicketTester.startPanel(new VisibilityAllowedBasedTestPanelSource(false, false));
		wicketTester.assertInvisible("panel:shy");
	}

	@Test
	public void visibleWithFirstContentVisibilityAllowed() {
		wicketTester.startPanel(new VisibilityAllowedBasedTestPanelSource(true, false));
		wicketTester.assertVisible("panel:shy");
	}

	@Test
	public void visibleWithSecondContentVisibilityAllowed() {
		wicketTester.startPanel(new VisibilityAllowedBasedTestPanelSource(false, true));
		wicketTester.assertVisible("panel:shy");
	}

	public class TestPanel extends Panel {

		private static final long serialVersionUID = 1L;

		public TestPanel(String id, ShyContainer shyContainer) {
			super(id);
			add(shyContainer);
		}

	}

	private abstract class TestPanelSource implements ITestPanelSource {
		private static final long serialVersionUID = 1L;

		protected boolean v1;
		protected boolean v2;

		public TestPanelSource(boolean v1, boolean v2) {
			super();
			this.v1 = v1;
			this.v2 = v2;
		}

		public Panel getTestPanel(String panelId) {
			ShyContainer shyContainer = new ShyContainer("shy");
			addComponent(shyContainer, new Label("c1", "Component 1"), v1);
			addComponent(shyContainer, new Label("c2", "Component 2"), v2);
			return new TestPanel(panelId, shyContainer);
		}


		protected void addComponent(ShyContainer shyContainer, Component component, boolean visibility) {
			setVisibility(component, visibility);
			shyContainer.add(component);
		}

		protected abstract void setVisibility(Component component, boolean visibility);
	}
	
	private class VisibilityBasedTestPanelSource extends TestPanelSource {
		private static final long serialVersionUID = 1L;

		public VisibilityBasedTestPanelSource(boolean v1, boolean v2) {
			super(v1, v2);
		}

		protected void setVisibility(Component component, boolean visibility) {
			component.setVisible(visibility);
		}
	}

	private class VisibilityAllowedBasedTestPanelSource extends TestPanelSource {
		private static final long serialVersionUID = 1L;

		public VisibilityAllowedBasedTestPanelSource(boolean v1, boolean v2) {
			super(v1, v2);
		}

		protected void setVisibility(Component component, boolean visibility) {
			component.setVisibilityAllowed(visibility);
		}
	}

}
