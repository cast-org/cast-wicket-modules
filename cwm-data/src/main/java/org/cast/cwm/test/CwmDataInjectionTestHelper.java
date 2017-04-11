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
package org.cast.cwm.test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.apache.wicket.model.Model;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IUserService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CwmDataInjectionTestHelper extends InjectionTestHelper {

	public CwmDataInjectionTestHelper() {
		super();
	}

	public ICwmSessionService injectAndStubCwmSessionService (CwmDataBaseTestCase baseInjectedTestCase) {
		ICwmSessionService cwmSessionService = injectMock(ICwmSessionService.class);
	    when(cwmSessionService.isSignedIn()).thenReturn(true);
	    when(cwmSessionService.getUserModel()).thenReturn(Model.of(baseInjectedTestCase.loggedInUser));
	    when(cwmSessionService.getUser()).thenReturn(baseInjectedTestCase.loggedInUser);
	    when(cwmSessionService.getCurrentPeriodModel()).thenReturn(Model.of(baseInjectedTestCase.period));
		return cwmSessionService;
	}

	public IUserService injectAndStubUserService (CwmDataBaseTestCase testCase) {
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

}
