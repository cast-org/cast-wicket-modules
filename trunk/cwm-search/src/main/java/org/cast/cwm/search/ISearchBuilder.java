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