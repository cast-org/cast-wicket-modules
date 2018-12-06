/*
 * Copyright 2011-2019 CAST, Inc.
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

import org.apache.wicket.mock.MockApplication;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;

import java.util.Collections;
import java.util.TreeSet;

/**
 * Base class for testing components that use cwm-data.
 *
 * This abstract class is intended to be extended into a base test case for your application.
 * You will need to override getInjectionHelper, with your own extended version of CwmDataInjectionTestHelper.
 * If your application keeps its HTML in a separate theme directory, also override
 * isApplicationThemed() to return true.
 *
 * This test case sets up injection and creates a pretend logged-in User with a Period.
 */
public abstract class CwmDataBaseTestCase<T extends CwmDataInjectionTestHelper> extends CwmBaseTestCase<T> {

	public Period period;
	public User loggedInUser;

	public CwmDataBaseTestCase() {
		super();
	}

	public void setUpData() {
		period = new Period();
		TestIdSetter.setId(Period.class, period, 1L);
		loggedInUser = makeUser(Role.STUDENT, period, "Mickey", "Mouse");
		TestIdSetter.setId(User.class, loggedInUser, 2L);
	}

	protected MockApplication newApplication() {
		CwmDataTestApplication app = new CwmDataTestApplication(injectionHelper.getMap());
		app.setApplicationUsesThemeDir(isApplicationThemed());
		return app;
	}

	public User makeUser(Role role, Period period, String firstName, String lastName) {
		User user = new User();
		user.setRole(role);
		user.setPeriods(new TreeSet<Period>(Collections.singletonList(period)));
		user.setFirstName(firstName);
		user.setLastName(lastName);
		return user;
	}

	@Override
	protected boolean isApplicationThemed() {
		return false;
	}

}
