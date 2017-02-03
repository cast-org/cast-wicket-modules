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
package org.cast.cwm.data.behavior;

import org.apache.wicket.markup.html.basic.Label;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.test.CwmBaseTestCase;
import org.cast.cwm.test.InjectionTestHelper;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class EventLoggingBehaviorTest extends CwmBaseTestCase<InjectionTestHelper> {

	private IEventService eventService;

	@Override
	public void populateInjection(InjectionTestHelper helper) {
		eventService = helper.injectMock(IEventService.class);

		ICwmSessionService cwmSessionService = helper.injectMock(ICwmSessionService.class);
		when(cwmSessionService.getUser()).thenReturn(new User(Role.STUDENT));
		when(cwmSessionService.isSignedIn()).thenReturn(true);
	}

	@Test
	public void canAttach() {
		Label p = new Label("test", "test");
		p.add(new EventLoggingBehavior("click", "testEvent"));
		tester.startComponentInPage(p);
	}

//	public void logsOnClick() {
//		// Hm.  We don't seem to have any way to test javascript based behavior.
//	}
	
	@Test
	public void logsAnEvent() {
		Label p = new Label("test", "test");
		EventLoggingBehavior behavior = new EventLoggingBehavior("click", "testEvent");
		behavior.setDetail("testDetail");
		p.add(behavior);

		tester.startComponentInPage(p);
		tester.executeBehavior(behavior);
		verify(eventService).storeEvent(eq(p), eq("testEvent"), eq("testDetail"));
	}

	@Override
	protected InjectionTestHelper getInjectionTestHelper() {
		return new InjectionTestHelper();
	}

	@Override
	protected boolean isApplicationThemed() {
		return false;
	}
}
