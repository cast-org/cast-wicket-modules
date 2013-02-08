package org.cast.cwm.admin;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class AdminHomeTest {

	private WicketTester tester;

	@Before
	public void setUp() {
		tester = new WicketTester();
	}
	
	@Test
	public void canRender() {
		tester.startPage(AdminHome.class);
	}
	
}
