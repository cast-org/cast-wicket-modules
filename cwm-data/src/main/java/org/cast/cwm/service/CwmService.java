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
package org.cast.cwm.service;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.wicket.model.IChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.cast.cwm.data.Initialization;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.init.IDatabaseInitializer;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class CwmService implements ICwmService {

	private static final Logger log = LoggerFactory.getLogger(CwmService.class);
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#confirmDatastoreModel(org.apache.wicket.model.IModel)
	 */
	@Override
	public void confirmDatastoreModel(IModel<? extends PersistedObject> objectModel) {
		if ((objectModel instanceof IChainingModel && !(((IChainingModel<? extends PersistedObject>) objectModel).getChainedModel() instanceof HibernateObjectModel))
				&& !(objectModel instanceof HibernateObjectModel))
			throw new IllegalStateException("This Service class expects HibernateObjectModels.");
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#getById(java.lang.Class, long)
	 */
	@Override
	public <T extends PersistedObject> IModel<T> getById(Class<T> clazz, long id) {
		return new HibernateObjectModel<T>(clazz, id);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#save(org.cast.cwm.data.PersistedObject)
	 */
	@Override
	public void save(PersistedObject object) {
		Databinder.getHibernateSession().save(object);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#delete(org.apache.wicket.model.IModel)
	 */
	@Override
	public void delete(IModel<? extends PersistedObject> objectModel) {
		confirmDatastoreModel(objectModel);
		Databinder.getHibernateSession().delete(objectModel.getObject());
		flushChanges();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#delete(org.cast.cwm.data.PersistedObject)
	 */
	@Override
	public void delete(PersistedObject object) {
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
			log.error("Can't ignore exception: {}", ex);
			ex.printStackTrace(System.err);
		} finally {
			session.beginTransaction();
		}
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#getInitializationNames()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> getInitializationNames() {
		Criteria criteria = Databinder.getHibernateSession().createCriteria(Initialization.class);
		criteria.setProjection(Projections.distinct(Projections.property("name")));
		return criteria.list();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ICwmService#saveInitialization(org.cast.cwm.data.init.IDatabaseInitializer)
	 */
	@Override
	public void saveInitialization (IDatabaseInitializer izer) {
		Initialization init = new Initialization();
		init.setName(izer.getName());
		init.setRunDate(new Date());
		Databinder.getHibernateSession().save(init);
		flushChanges();
	}

	@Override
	public JavaScriptResourceReference getLoglevelJavascriptResourceReference() {
		return new WebjarsJavaScriptResourceReference("github-com-pimterry-loglevel/current/loglevel.js");
	}

}
