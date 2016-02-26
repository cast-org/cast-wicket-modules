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
package org.cast.cwm.xml.component;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.cast.cwm.xml.handler.DefaultDynamicComponentHandler;
import org.cast.cwm.xml.handler.IDynamicComponentHandler;

/**
 * A  basic Component Resolver which can be constructed with a default handler and a list of handlers.
 * 
 * @see IDynamicComponentHandler, IDynamicComponentResolver
 * 
 * @author droby
 *
 */

@Getter
@Setter
public class BaseDynamicComponentResolver implements IDynamicComponentResolver {

	protected IDynamicComponentHandler defaultHandler;
	protected List<IDynamicComponentHandler> handlers;

	public BaseDynamicComponentResolver() {
		this(new DefaultDynamicComponentHandler(), new ArrayList<IDynamicComponentHandler>());
	}

	public BaseDynamicComponentResolver(List<IDynamicComponentHandler> handlers) {
		this(new DefaultDynamicComponentHandler(), handlers);
	}

	public BaseDynamicComponentResolver(
			IDynamicComponentHandler defaultHandler,
			List<IDynamicComponentHandler> handlers) {
		this.defaultHandler = defaultHandler;
		this.handlers = handlers;
	}
	
	public void addHandler(IDynamicComponentHandler handler) {
		getHandlers().add(handler);
	}

	@Override
	public IDynamicComponentHandler getHandler(String wicketId) {
		for (IDynamicComponentHandler handler: getHandlers()) {
			if (handler.canHandle(wicketId))
				return handler;
		}
		return getDefaultHandler();
	}

}