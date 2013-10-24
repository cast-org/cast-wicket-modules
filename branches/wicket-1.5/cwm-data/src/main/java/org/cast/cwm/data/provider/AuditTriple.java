package org.cast.cwm.data.provider;

import java.io.Serializable;

import org.hibernate.envers.RevisionType;
import org.hibernate.envers.tools.Triple;

/**
 * The type of data returned by audit queries.
 * Consists of a triple: the entity itself, the revision entity, and the revision type. 
 *
 * @param <E> the type of the wrapped entity
 * @param <R> the type of the revision entity
 */
public class AuditTriple<E extends Serializable,R extends Serializable> 
	extends Triple<E,R,RevisionType> 
	implements Serializable {

	private static final long serialVersionUID = 1L;

	public AuditTriple(E obj1, R obj2, RevisionType obj3) {
		super(obj1, obj2, obj3);
	}
	
	public E getEntity() {
		return getFirst();
	}
	
	public R getInfo() {
		return getSecond();
	}
	
	public RevisionType getType() {
		return getThird();
	}
}