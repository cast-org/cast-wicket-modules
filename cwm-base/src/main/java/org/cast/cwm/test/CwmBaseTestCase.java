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
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Base class for testing CWM-based application components.
 *
 * Allows for Guice injection and a "theme" directory of HTML files separate from
 * the java files.
 *
 * If you use cwm-data as well, see org.cast.cwm.test.CwmDataBaseTestCase in that package.
 */
public abstract class CwmBaseTestCase<T extends InjectionTestHelper> extends WicketTestCase {

	protected T injectionHelper;

	public CwmBaseTestCase() {
		super();
	}

	@Override
	public void commonBefore() {
		injectionHelper = getInjectionTestHelper();
		setUpData();
		populateInjection(injectionHelper);
		super.commonBefore();
	}

	@Override
	public WicketTester newWicketTester(final WebApplication app) {
		return new CwmWicketTester(app);
	}

	protected CwmWicketTester getWicketTester() {
		if (tester instanceof CwmWicketTester)
			return (CwmWicketTester) tester;
		System.err.println("Tester not of CwmWicketTester class: " + tester);
		return null;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected MockApplication newApplication() {
		CwmTestApplication app = new CwmTestApplication(injectionHelper.getMap());
		app.setApplicationUsesThemeDir(isApplicationThemed());
		return app;
	}
	
	public void setUpData() {
	}

	public void populateInjection(T helper) {
	}

	protected abstract boolean isApplicationThemed();
	
	protected abstract T getInjectionTestHelper();
}