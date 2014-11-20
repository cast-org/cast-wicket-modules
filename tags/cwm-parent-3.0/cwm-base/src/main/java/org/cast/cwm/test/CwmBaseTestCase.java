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

import org.apache.wicket.mock.MockApplication;
import org.junit.Before;

public abstract class CwmBaseTestCase {

	protected CwmWicketTester tester;
	protected InjectionTestHelper injectionHelper;

	public CwmBaseTestCase() {
		super();
	}

	@Before
	public void setup() throws Exception {
		injectionHelper = getInjectionTestHelper();
		setUpData();
		populateInjection();
		setUpTester();
	}

	public void setUpTester() throws Exception {
		tester = new CwmWicketTester(getTestApplication());		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected MockApplication getTestApplication() {
		if (isApplicationThemed())
			return new CwmTestThemedApplication(injectionHelper.getMap());
		else
			return new CwmTestApplication(injectionHelper.getMap());
	}
	
	public void setUpData() throws Exception {
	}

	public void populateInjection() throws Exception {
	}

	protected abstract boolean isApplicationThemed();
	
	protected abstract InjectionTestHelper getInjectionTestHelper();
}