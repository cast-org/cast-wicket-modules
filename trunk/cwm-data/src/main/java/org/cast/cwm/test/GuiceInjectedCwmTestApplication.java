/*
 * Copyright 2011-2013 CAST, Inc.
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

import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.DummyHomePage;

public class GuiceInjectedCwmTestApplication<T> extends GuiceInjectedTestApplication<T> {

	public GuiceInjectedCwmTestApplication(Map<Class<T>, T> injectionMap) {
		super(injectionMap);
	}
	
    @Override
    public void init() {
            super.init();
            // Check separate "theme" folder for markup and XSL styles.
            getResourceSettings().addResourceFolder(getThemeDir());
    }

    private String getThemeDir() {
            return "theme";
    }

    @Override
    public Class<? extends Page> getHomePage() {
            return DummyHomePage.class;
    }


}
