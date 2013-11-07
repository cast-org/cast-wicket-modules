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
package org.cast.cwm;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cast.cwm.data.IResponseType;

/**
 * ResponseTypeRegistry
 * 
 * A registry for the allowed response types for this application.
 * 
 * The registration of types should be done at application init.  The default initialization is done in
 * org.cast.cwm.CwmApplication.initResponseTypes(), which can be overridden by other applications.
 * 
 * This is injected as the default implementation of org.cast.cwm.IResponseTypeRegistry
 * 
 * @see IResponseTypeRegistry
 *
 * @author droby
 *
 */
public class ResponseTypeRegistry implements IResponseTypeRegistry {

	private static Map<String,IResponseType> legalResponseTypes = new ConcurrentHashMap<String,IResponseType>();

	/* (non-Javadoc)
	 * @see org.cast.cwm.IResponseTypeRegistry#registerResponseType((java.lang.String, org.cast.cwm.data.IResponseType)
	 */
	@Override
	public void registerResponseType(String name, IResponseType type) {
		legalResponseTypes.put(name, type);
		
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.IResponseTypeRegistry#getResponseType(java.lang.String)
	 */
	@Override
	public IResponseType getResponseType(String name) {
		return legalResponseTypes.get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.IResponseTypeRegistry#getLegalResposeTypes()
	 */
	@Override
	public Collection<IResponseType> getLegalResponseTypes() {
		return legalResponseTypes.values();
	}

}
