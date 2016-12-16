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
package org.cast.cwm.test;

import org.apache.wicket.mock.MockApplication;
import org.junit.Before;

/**
 * Base class for testing CWM-based application components.
 *
 * Allows for Guice injection and a "theme" directory of HTML files separate from
 * the java files.
 *
 * If you use cwm-data as well, see org.cast.cwm.test.CwmDataBaseTestCase in that package.
 */
public abstract class CwmBaseTestCase<T extends InjectionTestHelper>  {

	protected CwmWicketTester tester;
	protected T injectionHelper;

	public CwmBaseTestCase() {
		super();
	}

	@Before
	public void setup() throws Exception {
		injectionHelper = getInjectionTestHelper();
		setUpData();
		populateInjection(injectionHelper);
		setUpTester();
	}

	public void setUpTester() throws Exception {
		tester = new CwmWicketTester(getTestApplication());		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected MockApplication getTestApplication() {
		CwmTestApplication app = new CwmTestApplication(injectionHelper.getMap());
		app.setApplicationUsesThemeDir(isApplicationThemed());
		return app;
	}
	
	public void setUpData() throws Exception {
	}

	public void populateInjection(T helper) throws Exception {
	}

	protected abstract boolean isApplicationThemed();
	
	protected abstract T getInjectionTestHelper();
}