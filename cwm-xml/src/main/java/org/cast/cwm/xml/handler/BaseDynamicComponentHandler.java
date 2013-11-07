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
package org.cast.cwm.xml.handler;


/**
 * An abstract basic Component Handler specifying a wicketId prefix that it can handle.
 * The getComponent method is left to be implemented in the concrete implementation.
 * 
 * @see IDynamicComponentHandler
 * 
 * @author droby
 *
 */
public abstract class BaseDynamicComponentHandler  implements IDynamicComponentHandler {

	private String prefix;

	public BaseDynamicComponentHandler(String prefix) {
		super();
		this.prefix = prefix;
	}

	@Override
	public boolean canHandle(String wicketId) {
		return wicketId.startsWith(prefix);
	}

}