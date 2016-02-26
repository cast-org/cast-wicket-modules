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
package org.cast.cwm;

import java.util.Collection;

import org.cast.cwm.data.IResponseType;

/**
 * IResponseTypeRegistry
 * 
 * A registry for the allowed response types for this application.
 * 
 * @author droby
 *
 */
public interface IResponseTypeRegistry {

	/**
	 * Registers a response type for a name
	 * 
	 * @param name a String name used as lookup key
	 * @param type the associated response type
	 */
	void registerResponseType(String name, IResponseType type);

	/**
	 * getResponseType
	 * 
	 * @param name a String name used as lookup key
	 * @return the associated response type
	 * @throws IllegalArgumentException if the name is not a registered IResponseType
	 */
	IResponseType getResponseType(String name);

	/**
	 * getLegalResponseTypes
	 * 
	 * @return all registered response types
	 */
	Collection<IResponseType> getLegalResponseTypes();

}