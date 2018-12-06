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
package org.cast.cwm.search;

/**
 * Hibernate-search related service methods.
 * 
 * @author bgoldowsky
 *
 */
public interface ISearchService {
	
	/**
	 * Add or update the object in the search index.
	 * Hibernate Search automatically updates the index when changes are committed to an object,
	 * so this is not normally needed.  However, you may need it if you have index fields based on
	 * getter methods, and you know that the value returned by the getter will have changed 
	 * even though no changes have been made to the object itself.
	 *  
	 * @param object
	 */
	public void indexObject (Object object);

}
