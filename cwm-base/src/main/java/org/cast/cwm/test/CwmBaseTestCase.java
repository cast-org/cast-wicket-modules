package org.cast.cwm.test;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;

public abstract class CwmBaseTestCase {

	protected WicketTester tester;
	protected InjectionTestHelper injectionHelper;

	public CwmBaseTestCase() {
		super();
	}

	@Before
	public void setup() throws Exception {
		injectionHelper = getInjectionTestHelper();
		populateInjection();
		setUpTester();
	}

	public void setUpTester() throws Exception {
		tester = new CwmWicketTester(getTestApplication());		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private MockApplication getTestApplication() {
		return new CwmTestApplication(injectionHelper.getMap());
	}

	public void populateInjection() throws Exception {
	}

	protected abstract InjectionTestHelper getInjectionTestHelper();
}