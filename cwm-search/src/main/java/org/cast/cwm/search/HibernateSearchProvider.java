/*
 * Copyright 2011-2018 CAST, Inc.
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

import net.databinder.hib.Databinder;
import net.databinder.models.PropertyDataProvider;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.lucene.search.Query;
import org.apache.wicket.model.IModel;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.engine.spi.FacetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A result provider for a DataView of full-text search results.
 * Given an ISearchBuilder, it will configure and execute the search when needed.
 * 
 * @author bgoldowsky
 *
 * @param <T> the type of object that the search will return
 */
public class HibernateSearchProvider<T> extends PropertyDataProvider<T> {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(HibernateSearchProvider.class);
	
	/** The type of object that this search provider will return */
	protected Class<?> resultClass;
	
	protected ISearchBuilder builder;
	
	// Hold onto query during session; but it can't be serialized.
	private transient FullTextQuery query = null;
	// And remember session just so we can determine if query will still be valid.
	private transient Session lastSession = null;
	
	public HibernateSearchProvider(Class<?> resultClass, ISearchBuilder builder) {
		this.resultClass = resultClass;
		this.builder = builder;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<? extends T> iterator(long first, long count) {
		FullTextQuery query = getQuery();
		if (query == null) {
			List<T> empty = Collections.emptyList();
			return empty.iterator();
		}
		query.setFirstResult((int)first);
		query.setMaxResults((int)count);
		onBeforeSearchExecution(query);
		long startTime = System.currentTimeMillis();
		Iterator<? extends T> iterator = query.iterate();
		if (log.isDebugEnabled()) {
			log.debug("Query took {}ms, returned {} results", System.currentTimeMillis()-startTime, query.getResultSize());
		}
		return iterator;
	}

	/** Override this in a subclass to modify the query before it is run */
	protected void onBeforeSearchExecution(FullTextQuery query) {
	}
	
	@Override
	public long size() {
		FullTextQuery query = getQuery();
		return query == null ? 0 : query.getResultSize();
	}

	@Override
	protected IModel<T> dataModel(T object) {
		return new HibernateObjectModel<T>(object);
	}
	
	/**
	 * Return the FacetManager object for the current query.
	 * This can be used to extract facet counts and associated information.
	 */
	public FacetManager getFacetManager() {
		return getQuery().getFacetManager();
	}

	/**
	 * Return the current Query, from cache or newly built.
	 * If there are errors in creating the query, null may be returned.
	 */
	protected FullTextQuery getQuery() {
		Session session = Databinder.getHibernateSession();
		if (query == null || lastSession == null || !session.equals(lastSession)
				|| builder.isSearchUpdated()) {
			lastSession = session;
			FullTextSession fullTextSession = Search.getFullTextSession(session);
			QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(resultClass).get();
			Query luceneQuery;
			try {
				luceneQuery = builder.build (queryBuilder);
			} catch (Exception e) {
				// query building can fail e.g. if search string contains only stopwords.
				log.warn("Search query construction failed: {}", e);
				return null;
			}
			query = fullTextSession.createFullTextQuery(luceneQuery);
			builder.configure(queryBuilder, query);
		}
		return query;
	}

	public ISearchBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(ISearchBuilder builder) {
		this.builder = builder;
	}

}
