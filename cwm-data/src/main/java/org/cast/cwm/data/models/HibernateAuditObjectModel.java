/*
 * Copyright 2011-2020 CAST, Inc.
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
package org.cast.cwm.data.models;

import net.databinder.hib.Databinder;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

import java.io.Serializable;

/**
 * Model for a single historical state of an audited Hibernate object.
 * Detachable for efficient storage, but unlike HibernateObjectModel it keeps
 * both the ID and the revision information so that it can read the proper version
 * when later accessed.
 * 
 * @author bgoldowsky
 *
 * @param <T> model object type
 */
public class HibernateAuditObjectModel<T extends Serializable> extends LoadableDetachableModel<T> {

	private static final long serialVersionUID = 1L;
	
	private Class<T> objectClass;

	private long entityId;

	private int revision;

	public HibernateAuditObjectModel (Class<T> objectClass, long id, int revision) {
		this.objectClass = objectClass;
		this.entityId = id;
		this.revision = revision;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected T load() {
		AuditReader auditReader = AuditReaderFactory.get(Databinder.getHibernateSession());
		AuditQuery query = auditReader.createQuery()
				.forEntitiesAtRevision(objectClass, revision)
				.add(AuditEntity.id().eq(entityId));
		return (T) query.getSingleResult();	
	}
	
	@Override
	public void setObject(T object) {
		throw new UnsupportedOperationException("setObject not supported");
	}

}
