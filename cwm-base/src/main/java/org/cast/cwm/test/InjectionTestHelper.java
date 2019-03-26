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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class InjectionTestHelper {

	protected Map<Class<?>, Object> injectionMap;

	public InjectionTestHelper() {
		super();
		injectionMap = new HashMap<>();
	}

	public <T> T injectMock(Class<T> clazz) {
		return injectObject(clazz, mock(clazz));
	}

	public <T> T injectObject(Class<T> clazz, T instance) {
		injectionMap.put(clazz, instance);
		return instance;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz) {
		return (T) injectionMap.get(clazz);
	}

	public Map<Class<?>, Object> getMap() {
		return injectionMap;
	}

}