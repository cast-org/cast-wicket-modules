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
package org.cast.cwm.service;

import org.cast.cwm.data.IContentType;

/**
 * Methods for working with UserContent and IContentType.
 *
 * Since there is no universal list of content types,
 * This has no default implementation.  One must be defined by the application.
 *
 */
public interface IUserContentService {

	/**
	 * Return the enumeration used for content types by the application.
	 * @return IContentType class object
	 */
	Class<? extends IContentType> getContentTypeClass();

	/**
	 * Look up the name and return the IEventType object that it represents.
	 * 
	 * @param name a String name used as lookup key
	 * @return the associated response type
	 * @throws IllegalArgumentException if the name is not a registered IContentType
	 */
	IContentType getContentType(String name);

}