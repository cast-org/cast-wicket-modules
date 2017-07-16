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
package org.cast.cwm.data.component;

import org.cast.cwm.data.Role;
import org.cast.cwm.test.CwmDataBaseTestCase;
import org.cast.cwm.test.CwmDataInjectionTestHelper;
import org.cast.cwm.test.CwmDataTestCase;
import org.junit.Test;

public class RoleVisibilityContainerTest extends CwmDataTestCase {

	@Override
	public void populateInjection(CwmDataInjectionTestHelper helper) {
		super.populateInjection(helper);
		helper.injectAndStubCwmSessionService(this);
	}

	@Test
	public void canRender() {
		tester.startComponentInPage(new RoleVisibilityContainer("id", Role.GUEST, true));
		tester.assertComponent("id", RoleVisibilityContainer.class);
	}

	@Test
	public void hiddenFromStudentWhenTargetedToTeacher() {
		tester.startComponentInPage(new RoleVisibilityContainer("id", Role.TEACHER, true));
		tester.assertInvisible("id");
	}

	@Test
	public void visibleToTeacherWhenTargetedToTeacher() {
		loggedInUser.setRole(Role.TEACHER);
		tester.startComponentInPage(new RoleVisibilityContainer("id", Role.TEACHER, true));
		tester.assertComponent("id", RoleVisibilityContainer.class);
	}

	@Test
	public void visibleToResearcherWhenTargetedToTeacherLoosely() {
		loggedInUser.setRole(Role.RESEARCHER);
		tester.startComponentInPage(new RoleVisibilityContainer("id", Role.TEACHER, true));
		tester.assertComponent("id", RoleVisibilityContainer.class);
	}

	@Test
	public void hiddenFromResearcherWhenTargetedToTeacherStrictly() {
		loggedInUser.setRole(Role.RESEARCHER);
		tester.startComponentInPage(new RoleVisibilityContainer("id", Role.TEACHER, false));
		tester.assertInvisible("id");
	}

}
