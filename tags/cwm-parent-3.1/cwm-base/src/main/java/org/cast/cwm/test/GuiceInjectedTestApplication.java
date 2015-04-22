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

import java.util.Map;

/**
 * @deprecated  Use  {@link #CwmTestApplication} for a fake application with injection (which may be empty)
 *  or {@link #CwmTestThemedApplication} for a fake application injection that also uses the usual CAST theme directory.
 */
@Deprecated
public class GuiceInjectedTestApplication<T> extends CwmTestApplication<T> {

	public GuiceInjectedTestApplication(Map<Class<T>, T> injectionMap) {
		super(injectionMap);
	}

}
