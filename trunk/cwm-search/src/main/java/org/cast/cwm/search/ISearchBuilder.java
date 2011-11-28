/*
 * Copyright 2011 CAST, Inc.
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

import java.io.Serializable;

import org.apache.lucene.search.Query;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * An object that can construct a full-text query.
 * 
 * ISearchBuilder and HibernateSearchProvider are a pair analogous to Databinder's
 * QueryBuilder and HibernateProvider, but allow for full-text search queries
 * rather than only SQL queries.
 * 
 * Do not confuse the two classes {@link net.databinder.models.hib.QueryBuilder} 
 * and {@link org.hibernate.search.query.dsl.QueryBuilder}, they are quite different.
 *
 * @author bgoldowsky
 *
 */
public interface ISearchBuilder extends Serializable {
	
	/**
	 * Should return true if the search parameters have been changed since 
	 * the last time that the build() method was called.  This will
	 * let the caller know that it needs to call build() again.
	 * 
	 * @return true if search terms have changed.
	 */
	public abstract boolean isSearchUpdated();

	/**
	 * This method will be called to create a Query when it is about to be executed.
	 * @param builder the Hibernate Search QueryBuilder object
	 * @return constructed Query object.
	 */
	public abstract Query build(QueryBuilder b);

	/**
	 * Perform any necessary configuration of the query after it is created.
	 * This normally includes steps such as binding values to query parameters
	 * or enabling faceting requests.
	 * @param queryBuilder the Hibernate Search QueryBuilder object
	 * @param query the constructed FullTextQuery in need of configuration
	 */
	public abstract void configure(QueryBuilder queryBuilder, FullTextQuery query);

}