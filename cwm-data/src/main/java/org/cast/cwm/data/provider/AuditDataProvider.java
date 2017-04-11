/*
 * Copyright 2011-2017 CAST, Inc.
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
package org.cast.cwm.data.provider;

import net.databinder.hib.Databinder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.builders.ISortableAuditQueryBuilder;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditProperty;
import org.hibernate.envers.query.internal.property.RevisionNumberPropertyName;

import java.io.Serializable;
import java.util.Iterator;

/**
 * A sortable DataProvider for views of audit data.
 * Can be constructed with any {@link ISortableAuditQueryBuilder}
 * 
 * @author bgoldowsky
 *
 * @param <E> type of the @Audited @Entity being queried.
 * @param <R> type of the @RevisionEntity in your app.
 * 
 * TODO: allow parameterization of sort field type
 */
public  class AuditDataProvider<E extends Serializable, R extends Serializable> 
		implements ISortableDataProvider<AuditTriple<E,R>,String> {

	private static final long serialVersionUID = 1L;
	private ISortableAuditQueryBuilder builder;

	public AuditDataProvider (ISortableAuditQueryBuilder builder) {
		this.builder = builder;
	}


	@SuppressWarnings("unchecked")
	@Override
	public Iterator<? extends AuditTriple<E, R>> iterator(long first, long count) {
		AuditQuery query = builder.buildSorted(Databinder.getHibernateSession());
		query.setFirstResult((int)first);
		query.setMaxResults((int)count);
		return new AuditIteratorAdapter<E,R>(query.getResultList().iterator());
	}

	@Override
	public long size() {
		AuditQuery query = builder.build(Databinder.getHibernateSession());
		query.addProjection(new AuditProperty<Long>("auditAlias", new RevisionNumberPropertyName()).count());
		Number size = (Number) query.getSingleResult();
		return size == null ? 0 : size.longValue();
	}

	@Override
	public IModel<AuditTriple<E,R>> model(AuditTriple<E,R> object) {
		return new Model<AuditTriple<E,R>>(object);
	}

	@Override
	public ISortState<String> getSortState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void detach() {
		if (builder!=null && builder instanceof IDetachable)
			((IDetachable)builder).detach();
	}


	private static class AuditIteratorAdapter<E extends Serializable,R extends Serializable> 
			implements Iterator<AuditTriple<E,R>> {

		private Iterator<Object[]> delegate;

		public AuditIteratorAdapter (Iterator<Object[]> iterator) {
			this.delegate = iterator;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		@SuppressWarnings("unchecked")
		public AuditTriple<E, R> next() {
			Object[] array = delegate.next();
			E entity = (E)array[0];
			R revision = (R)array[1];
			RevisionType type = (RevisionType)array[2];
			return new AuditTriple<E,R>(entity, revision, type);
		}

		@Override
		public void remove() {
			delegate.remove();
		}

	}
}
