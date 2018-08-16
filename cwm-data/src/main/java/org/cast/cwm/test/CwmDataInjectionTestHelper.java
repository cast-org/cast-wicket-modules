/*
 * Copyright 2011-2018 CAST, Inc.
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
package org.cast.cwm.test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.cast.cwm.IAppConfiguration;
import org.cast.cwm.data.User;
import org.cast.cwm.figuration.service.IFigurationService;
import org.cast.cwm.service.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CwmDataInjectionTestHelper extends InjectionTestHelper {

	public CwmDataInjectionTestHelper() {
		super();
	}

	public IAppConfiguration injectAppConfiguration(CwmDataBaseTestCase testCase) {
		IAppConfiguration mock = injectMock(IAppConfiguration.class);
		return mock;
	}

	public ICwmService injectCwmService(CwmDataBaseTestCase testCase) {
		ICwmService mock = injectMock(ICwmService.class);
		when(mock.getLoglevelJavascriptResourceReference())
				.thenReturn(new JavaScriptResourceReference(ICwmService.class, "logLevel"));
		return mock;
	}

	public ICwmSessionService injectCwmSessionService(CwmDataBaseTestCase testCase) {
		ICwmSessionService cwmSessionService = injectMock(ICwmSessionService.class);
	    when(cwmSessionService.isSignedIn()).thenReturn(true);
	    when(cwmSessionService.getUserModel()).thenReturn(Model.of(testCase.loggedInUser));
	    when(cwmSessionService.getUser()).thenReturn(testCase.loggedInUser);
	    when(cwmSessionService.getCurrentPeriodModel()).thenReturn(Model.of(testCase.period));
		return cwmSessionService;
	}

	public IUserService injectUserService(CwmDataBaseTestCase testCase) {
		IUserService userService = injectMock(IUserService.class);
		when(userService.newUser()).thenAnswer(new Answer<User>() {
			@Override
			public User answer(InvocationOnMock invocation) throws Throwable {
				return new User();
			}
		});
		when(userService.getByEmail(anyString())).thenReturn(Model.of((User)null));
		when(userService.getByUsername(anyString())).thenReturn(Model.of((User) null));
		when(userService.getBySubjectId(anyString())).thenReturn(Model.of((User) null));
		return userService;
	}

	public IEventService injectEventService (CwmDataBaseTestCase testCase) {
		IEventService eventService = injectMock(IEventService.class);
		return eventService;
	}

	public IFigurationService injectFigurationService(CwmDataBaseTestCase testCase) {
	    return injectMock(IFigurationService.class);
    }

}
