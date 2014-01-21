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
package org.cast.cwm.data.component;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.tester.FormTester;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.test.CwmWicketTester;
import org.cast.cwm.test.GuiceInjectedTestApplication;
import org.cast.cwm.test.TestDataUtil;
import org.junit.Before;
import org.junit.Test;

public class PeriodChoicePanelTest {

	private CwmWicketTester tester;
	private ArrayList<Period> periods;
	private User mockUser;
	private ICwmSessionService cwmSessionService;

	@Getter
	private Period currentPeriod;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() {
		setUpData();

		HashMap<Class<? extends Object>, Object> injectionMap = new HashMap<Class<? extends Object>, Object>();
        populateInjectionMap(injectionMap);
		tester = new CwmWicketTester(new GuiceInjectedTestApplication(injectionMap));		
	}

	private void setUpData() {
		periods = new ArrayList<Period>();
		Period p = new Period();
		TestDataUtil.setId(p, 1L);
		p.setName("Period A");
		periods.add(p);
		p = new Period();
		TestDataUtil.setId(p, 2L);
		p.setName("Period B");
		periods.add(p);
		
		currentPeriod = periods.get(0);
		
		mockUser = mock(User.class);
		when(mockUser.getPeriodsAsList()).thenReturn(periods);
	}
	
	private void populateInjectionMap(HashMap<Class<? extends Object>, Object> injectionMap) {
		cwmSessionService = mock(ICwmSessionService.class);
        when(cwmSessionService.getCurrentPeriodModel()).thenReturn(new PropertyModel<Period>(this, "currentPeriod"));
        when(cwmSessionService.getUserModel()).thenReturn(Model.of(mockUser));
        injectionMap.put(ICwmSessionService.class, cwmSessionService);
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
		tester.assertModelValue("panel:selectForm:periodChoice", periods.get(1));
		assertEquals("Session current period wasn't set to new value", periods.get(1), cwmSessionService.getCurrentPeriodModel().getObject());
		// Not verifying call to sessionservice setter, since it's already set by virtue of sharing a model.
	}
	
}
