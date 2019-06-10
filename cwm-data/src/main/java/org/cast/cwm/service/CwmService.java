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
package org.cast.cwm.service;

import com.google.inject.Inject;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.wicket.model.IChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.cast.cwm.data.Initialization;
import org.cast.cwm.data.init.IDatabaseInitializer;
import org.cast.cwm.db.data.PersistedObject;
import org.cast.cwm.db.service.IDBService;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;

import java.util.Date;
import java.util.List;

public class CwmService implements ICwmService {

	@Inject
	private IDBService dbService;
	
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
		dbService.flushChanges();
	}

	@Override
	public JavaScriptResourceReference getLoglevelJavascriptResourceReference() {
		return new WebjarsJavaScriptResourceReference("github-com-pimterry-loglevel/current/loglevel.js");
	}

}
