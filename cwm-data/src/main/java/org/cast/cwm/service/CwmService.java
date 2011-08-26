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
package org.cast.cwm.service;

import java.util.Date;
import java.util.List;

import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.model.IChainingModel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Initialization;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.init.IDatabaseInitializer;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CwmService {

	private static final Logger log = LoggerFactory.getLogger(CwmService.class);
	
	protected static CwmService instance = new CwmService();

	public static CwmService get() {
		return instance;
	}

	public static void setInstance(CwmService inst) {
		CwmService.instance = inst;
	}
	
	/**
	 * Checks the model wrapping a {@link PersistedObject} to confirm that
	 * it has properly been implemented to work with the underlying datastore.
	 * 
	 * Override this method to provide your own custom implementation.
	 * @param objectModel
	 */
	public void confirmDatastoreModel(IModel<? extends PersistedObject> objectModel) {
		if ((objectModel instanceof IChainingModel && !(((IChainingModel<? extends PersistedObject>) objectModel).getChainedModel() instanceof HibernateObjectModel))
				&& !(objectModel instanceof HibernateObjectModel))
			throw new IllegalStateException("This Service class expects HibernateObjectModels.");
	}
	
	/**
	 * Look up a datastore object by its ID.  This method is implemented using
	 * the underlying datastore system.
	 * 
	 */
	public <T extends PersistedObject> IModel<T> getById(Class<T> clazz, long id) {
		return new HibernateObjectModel<T>(clazz, id);
	}
	
	/**
	 * Delete a {@link PersistedObject} from the datastore.
	 * 
	 * @param objectModel
	 */
	public void delete(IModel<? extends PersistedObject> objectModel) {
		confirmDatastoreModel(objectModel);
		Databinder.getHibernateSession().delete(objectModel.getObject());
		CwmService.get().flushChanges();
	}
	
	/**
	 * Flush changes to the datastore.  Essentially, this commits the previous
	 * transaction and starts a new transaction.  This should be run at the end of
	 * any Service method that is making changes to the datastore.
	 */
	public void flushChanges() {
		flushChanges(false);
	}
	
	/**
	 * <p>
	 * Flush changes to the datastore.  Essentially, this commits the previous
	 * transaction and starts a new transaction.  This should be run at the end of
	 * any Service method that is making changes to the datastore.
	 * </p>
	 * 
	 * <p>
	 * If catchErrors is true, the commit will be run in a <em>try</em> block
	 * and any exceptions will be ignored.
	 * </p>
	 * 
	 * @param catchErrors
	 */
	public void flushChanges(boolean catchErrors) {
		
		Session session = Databinder.getHibernateSession();
		try {
			session.flush(); // Modified from example in DataForm
			session.getTransaction().commit();
			
		} catch (HibernateException ex) {
			if (catchErrors) {
				// Note: Hibernate Logging will often print the stack trace anyways
				session.getTransaction().rollback();
				log.debug("Ignored exception during commit: {}", ex.getMessage());
			} else {
				throw ex;
			}
		} finally {
			session.beginTransaction();
		}
	}

	/**
	 * Return a list of all distinct initializers that have been run.
	 * @return list of names of initializers that have at some point been run.
	 */
	@SuppressWarnings("unchecked")
	public List<String> getInitializationNames() {
		Criteria criteria = Databinder.getHibernateSession().createCriteria(Initialization.class);
		criteria.setProjection(Projections.distinct(Projections.property("name")));
		return criteria.list();
	}
	
	/**
	 * Record an initialization record in the database.
	 * @param initializer the database initializer that was run.
	 */
	public void saveInitialization (IDatabaseInitializer izer) {
		Initialization init = new Initialization();
		init.setName(izer.getName());
		init.setRunDate(new Date());
		Databinder.getHibernateSession().save(init);
		flushChanges();
	}
	
}
