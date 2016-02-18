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
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.mock.MockApplication;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class CwmTestApplication<T> extends MockApplication {

	protected Map<Class<T>, T> injectionMap;

	public CwmTestApplication() {
		this(new HashMap<Class<T>, T>());
	}
	
	public CwmTestApplication(Map<Class<T>, T> injectionMap) {
		this.injectionMap = injectionMap;
	}

	@Override
	public void init() {
		super.init();
		getDebugSettings().setDevelopmentUtilitiesEnabled(false);
		getComponentInstantiationListeners().add(new GuiceComponentInjector(this, getGuiceInjector()));
	}

	protected Injector getGuiceInjector() {
		return Guice.createInjector(new AbstractModule(){
			@Override
			protected void configure() {
				for (Class<T> keyClass: injectionMap.keySet()) {
					bind(keyClass).toInstance(injectionMap.get(keyClass));
				}
			}
		});
	}

}