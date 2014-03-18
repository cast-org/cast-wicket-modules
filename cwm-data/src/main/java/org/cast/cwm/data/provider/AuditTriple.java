/*
 * Copyright 2011-2013 CAST, Inc.
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

import java.io.Serializable;

import lombok.Data;

import org.hibernate.envers.RevisionType;

/**
 * The type of data returned by audit queries.
 * Consists of a triple: the entity itself, the revision entity, and the revision type. 
 *
 * @param <E> the type of the wrapped entity
 * @param <R> the type of the revision entity
 */
@Data
public class AuditTriple<E extends Serializable,R extends Serializable> 
	implements Serializable {

	private static final long serialVersionUID = 1L;
	
	E entity;	
	R info;
	RevisionType type;

	public AuditTriple(E obj1, R obj2, RevisionType obj3) {
		entity = obj1;
		info = obj2;
		type = obj3;
	}
	
}