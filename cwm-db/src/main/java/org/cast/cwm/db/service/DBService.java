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
package org.cast.cwm.db.service;

import net.databinder.hib.Databinder;
import net.databinder.hib.SessionUnit;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.db.data.PersistedObject;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * An injectable service for interacting with Hibernate and cwm-db classes.
 * 
 * At the moment, this is just a facade in front of a few static methods provided 
 * by the Databinder class.  However, it may eventually become the primary way to 
 * access database functions.
 * 
 * @author bgoldowsky
 *
 */
public class DBService implements IDBService {

	private static final Logger log = LoggerFactory.getLogger(DBService.class);

	public DBService() {
	}

	@Override
	public Session getHibernateSession() {
		return Databinder.getHibernateSession();
	}

	@Override
	public Object ensureHibernateSession(SessionUnit unit) {
		return Databinder.ensureSession(unit);
	}

	@Override
	public Serializable save(Object persistableObject) {
		return getHibernateSession().save(persistableObject);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#getById(java.lang.Class, long)
	 */
	@Override
	public <T extends PersistedObject> IModel<T> getById(Class<T> clazz, long id) {
		return new HibernateObjectModel<>(clazz, id);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#delete(org.apache.wicket.model.IModel)
	 */
	@Override
	public void delete(IModel<?> objectModel) {
		delete(objectModel.getObject());
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#delete(org.cast.cwm.db.data.PersistedObject)
	 */
	@Override
	public void delete(Object object) {
		Databinder.getHibernateSession().delete(object);
		flushChanges();
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#flushChanges()
	 */
	@Override
	public void flushChanges() {
		flushChanges(false);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#flushChanges(boolean)
	 */
	@Override
	public void flushChanges(boolean catchErrors) {

		Session session = Databinder.getHibernateSession();
		try {
			session.flush(); // Modified from example in DataForm
			session.getTransaction().commit();

		} catch (HibernateException ex) {
			session.getTransaction().rollback();
			if (catchErrors) {
				// Note: Hibernate Logging will often print the stack trace anyways
				log.info("Ignored exception during commit: {}", ex.getMessage());
			} else {
				throw ex;
			}
		} catch (Exception ex) {
			session.getTransaction().rollback();
			log.error("Can't ignore exception: {}", ex.getMessage());
			ex.printStackTrace(System.err);
		} finally {
			session.beginTransaction();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T initializeAndUnproxy(T entity) {
		if (entity == null) {
			throw new
					NullPointerException("Entity passed for initialization is null");
		}

		Hibernate.initialize(entity);
		if (entity instanceof HibernateProxy) {
			entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
					.getImplementation();
		}
		return entity;
	}
}
