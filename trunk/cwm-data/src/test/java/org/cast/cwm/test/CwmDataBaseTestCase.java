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
