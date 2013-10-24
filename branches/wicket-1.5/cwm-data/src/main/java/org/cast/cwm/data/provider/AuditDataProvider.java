package org.cast.cwm.data.provider;

import java.io.Serializable;
import java.util.Iterator;

import net.databinder.hib.Databinder;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.builders.ISortableAuditQueryBuilder;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditProperty;
import org.hibernate.envers.query.property.RevisionNumberPropertyName;

/**
 * A sortable DataProvider for views of audit data.
 * Can be constructed with any {@link ISortableAuditQueryBuilder}
 * 
 * @author bgoldowsky
 *
 * @param <E> type of the @Audited @Entity being queried.
 * @param <R> type of the @RevisionEntity in your app.
 */
public  class AuditDataProvider<E extends Serializable, R extends Serializable> 
		implements ISortableDataProvider<AuditTriple<E,R>> {

	private static final long serialVersionUID = 1L;
	private ISortableAuditQueryBuilder builder;

	public AuditDataProvider (ISortableAuditQueryBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void detach() {
		// TODO Auto-generated method stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<? extends AuditTriple<E, R>> iterator(int first, int count) {
		AuditQuery query = builder.buildSorted(Databinder.getHibernateSession());
		query.setFirstResult(first);
		query.setMaxResults(count);
		return new AuditIteratorAdapter<E,R>(query.getResultList().iterator());
	}

	@Override
	public int size() {
		AuditQuery query = builder.build(Databinder.getHibernateSession());
		query.addProjection(new AuditProperty<Long>(new RevisionNumberPropertyName()).count());
		Number size = (Number) query.getSingleResult();
		return size == null ? 0 : size.intValue();
	}

	@Override
	public IModel<AuditTriple<E,R>> model(AuditTriple<E,R> object) {
		return new Model<AuditTriple<E,R>>(object);
	}

	@Override
	public ISortState getSortState() {
		// TODO Auto-generated method stub
		return null;
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
