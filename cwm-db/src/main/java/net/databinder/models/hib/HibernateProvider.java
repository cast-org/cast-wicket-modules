/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2008  Nathan Hamblen nathan@technically.us
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.databinder.models.hib;

import java.util.Iterator;

import net.databinder.hib.Databinder;
import net.databinder.models.PropertyDataProvider;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;

/**
 * Provides query results to DataView and related components. Like the Hibernate model classes,
 * the results of this provider can be altered by query binders and criteria builders. By default
 * this provider wraps items in a compound property model in addition to a Hibernate model.
 * This is convenient for mapping DataView subcomponents as bean properties (as with
 * PropertyListView). However, <b>DataTable will not work with a compound property model.</b>
 * Call setWrapWithPropertyModel(false) when using with DataTable, DataGridView, or any
 * other time you do not want a compound property model.
 * @author Nathan Hamblen
 */
public class HibernateProvider<T> extends PropertyDataProvider<T> {
	private static final long serialVersionUID = 1L;
	private Class objectClass;
	private ICriteriaBuilder criteriaBuilder;
	private QueryBuilder queryBuilder, countQueryBuilder;
	
	private Object factoryKey;
	
	/**
	 * Provides all entities of the given class.
	 */
	public HibernateProvider(Class objectClass) {
		this.objectClass = objectClass;
	}

	/**
	 * Provides all entities of the given class.
	 * @param objectClass
	 * @param criteriaBuider builds different criteria objects for iterator() and size()
	 */
	public HibernateProvider(Class objectClass, ICriteriaBuilder criteriaBuider) {
		this(objectClass);
		this.criteriaBuilder = criteriaBuider;
	}

	/**
	 * Provides entities matching the given queries.
	 */
	public HibernateProvider(final String query, final String countQuery) {
		this(new QueryBinderBuilder(query), new QueryBinderBuilder(countQuery));
	}
	
	public HibernateProvider(QueryBuilder queryBuilder, QueryBuilder countQueryBuilder) {
		this.queryBuilder = queryBuilder;
		this.countQueryBuilder = countQueryBuilder;
	}

	/** @return session factory key, or null for the default factory */
	public Object getFactoryKey() {
		return factoryKey;
	}

	/**
	 * Set a factory key other than the default (null).
	 * @param key session factory key
	 * @return this, for chaining
	 */
	public HibernateProvider<T> setFactoryKey(Object key) {
		this.factoryKey = key;
		return this;
	}
	
	/**
	 * It should not normally be necessary to override (or call) this default implementation.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Iterator<T> iterator(long first, long count) {
		Session sess =  Databinder.getHibernateSession(factoryKey);
		
		if(queryBuilder != null) {
			org.hibernate.Query q = queryBuilder.build(sess);
			q.setFirstResult((int)first);
			q.setMaxResults((int)count);
			return q.iterate();
		}			
		
		Criteria crit = sess.createCriteria(objectClass);
		if (criteriaBuilder != null)
			criteriaBuilder.buildOrdered(crit);
		
		crit.setFirstResult((int)first);
		crit.setMaxResults((int)count);
		return crit.list().iterator();
	}
	
	/**
	 * Only override this method if a single count query or 
	 * criteria projection is not possible.
	 */
	@Override
	public long size() {
		Session sess =  Databinder.getHibernateSession(factoryKey);

		if(countQueryBuilder != null) {
			org.hibernate.Query q = countQueryBuilder.build(sess);
			Object obj = q.uniqueResult();
			return ((Number) obj).longValue();
		}
		
		Criteria crit = sess.createCriteria(objectClass);
		
		if (criteriaBuilder != null)
			criteriaBuilder.buildUnordered(crit);
		crit.setProjection(Projections.rowCount());
		Number size = (Number) crit.uniqueResult();
		return size == null ? 0 : size.longValue();
	}


	@Override
	protected IModel<T> dataModel(T object) {
		return new HibernateObjectModel<T>(object);
	}
	
	/** Detach the QueryBuilder or ICriteriaBuilder if needed. */
	@Override
	public void detach() {
		if (queryBuilder != null && queryBuilder instanceof IDetachable)
			((IDetachable)queryBuilder).detach();
		if (criteriaBuilder != null && criteriaBuilder instanceof IDetachable)
			((IDetachable)criteriaBuilder).detach();
	}
}
