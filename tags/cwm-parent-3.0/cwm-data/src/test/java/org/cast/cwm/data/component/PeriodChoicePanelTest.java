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
package org.cast.cwm.data.component;

import static org.junit.Assert.assertEquals;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.wicket.util.tester.FormTester;
import org.cast.cwm.data.Period;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.test.CwmDataBaseTestCase;
import org.cast.cwm.test.TestDataUtil;
import org.junit.Test;

public class PeriodChoicePanelTest extends CwmDataBaseTestCase {

	private Period period2;
	private SortedSet<Period> periods;

	private ICwmSessionService cwmSessionService;

	@Override
	public void setUpData() {
		super.setUpData();
		periods = new TreeSet<Period>();
		periods.add(period);
		period.setName("Period A");
		period2 = new Period();
		TestDataUtil.setId(period2, 2L);
		period2.setName("Period B");
		periods.add(period2);

		loggedInUser.setPeriods(periods);
	}
	
	@Override
	public void populateInjection() {
		cwmSessionService = getHelper().injectAndStubCwmSessionService(this);
	}

	@Test
	public void canRender() {
		tester.startComponentInPage(new PeriodChoicePanel("panel"));
		tester.assertComponent("panel", PeriodChoicePanel.class);
		tester.assertContains("Period A");
	}
	
	@Test
	public void submitSetsPeriod() {
		tester.startComponentInPage(new PeriodChoicePanel("panel"));
		FormTester ft = tester.newFormTester("panel:selectForm", false);
		ft.select("periodChoice", 1);
		ft.submit("view");
		tester.clickLink("panel:selectForm:view");
		tester.assertModelValue("panel:selectForm:periodChoice", period2);
		assertEquals("Session current period wasn't set to new value", period2, cwmSessionService.getCurrentPeriodModel().getObject());
		// Not verifying call to sessionservice setter, since it's already set by virtue of sharing a model.
	}
	
}
