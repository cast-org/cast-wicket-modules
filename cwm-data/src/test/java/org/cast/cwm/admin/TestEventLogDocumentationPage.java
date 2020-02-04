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
package org.cast.cwm.admin;

import org.cast.cwm.TestingEventType;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.test.CwmDataInjectionTestHelper;
import org.cast.cwm.test.CwmDataTestCase;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class TestEventLogDocumentationPage extends CwmDataTestCase {

	private IEventService eventService;

	@Override
	public void populateInjection(CwmDataInjectionTestHelper helper) {
		super.populateInjection(helper);
		helper.injectAppConfiguration(this);
		helper.injectCwmSessionService(this);
		helper.injectFigurationService(this);

		eventService = helper.injectEventService(this);
		when(eventService.listEventTypes()).thenAnswer(new Answer<List<TestingEventType>>() {
			@Override
			public List<TestingEventType> answer(InvocationOnMock invocation) {
				return Arrays.asList(TestingEventType.values());
			}
		});

	}
	
	@Test
	public void canRender() {
		tester.startPage(EventLogDocumentationPage.class);
		tester.assertRenderedPage(EventLogDocumentationPage.class);
		tester.assertContains("Event Types");
		tester.assertLabel("type:1:name", "LOGIN");
		tester.assertLabel("type:1:displayName", "login");
		tester.assertLabel("type:1:documentation", "Documentation for LOGIN");
	}
	
}
