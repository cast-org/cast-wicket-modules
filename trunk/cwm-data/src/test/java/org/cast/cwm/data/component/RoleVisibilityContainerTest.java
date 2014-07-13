package org.cast.cwm.data.component;

import org.cast.cwm.data.Role;
import org.cast.cwm.test.CwmDataBaseTestCase;
import org.cast.cwm.test.CwmDataInjectionTestHelper;
import org.junit.Test;

public class RoleVisibilityContainerTest extends CwmDataBaseTestCase {

	@Override
	public void populateInjection() throws Exception {
		super.populateInjection();
		CwmDataInjectionTestHelper helper = getHelper();
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
