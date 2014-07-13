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
package org.cast.cwm.test;

import java.util.Arrays;
import java.util.TreeSet;

import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;

public class CwmDataBaseTestCase extends CwmBaseTestCase {

	public Period period;
	public User loggedInUser;

	public CwmDataBaseTestCase() {
		super();
	}

	@Override
	protected InjectionTestHelper getInjectionTestHelper() {
		return new CwmDataInjectionTestHelper();
	}
	
	protected CwmDataInjectionTestHelper getHelper() {
		return (CwmDataInjectionTestHelper) injectionHelper;
	}
	
	public void setUpData() {
		period = new Period();
		TestIdSetter.setId(Period.class, period, 1L);
		loggedInUser = makeUser(Role.STUDENT, period, "Mickey", "Mouse");
		TestIdSetter.setId(User.class, loggedInUser, 2L);
	}

	public User makeUser(Role role, Period period, String firstName, String lastName) {
		User user = new User();
		user.setRole(role);
		user.setPeriods(new TreeSet<Period>(Arrays.asList(period)));
		user.setFirstName(firstName);
		user.setLastName(lastName);
		return user;
	}

	@Override
	protected boolean isApplicationThemed() {
		return false;
	}

}
