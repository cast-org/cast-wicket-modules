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
package org.cast.cwm.service;

import com.google.inject.Inject;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.*;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.builders.PeriodCriteriaBuilder;
import org.cast.cwm.data.component.HibernateEditPeriodForm;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * General Service Class for both Sites and Periods  
 */
public class SiteService implements ISiteService {

	@Inject
	private ICwmService cwmService;

	// Site specific methods
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#getSiteClass()
	 */
	@Override
	public Class<? extends Site> getSiteClass() {
		return Site.class;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#newSite()
	 */
	@Override
	public final Site newSite() {
		try {
			return getSiteClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}	
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#listSites()
	 */
	@Override
	public IModel<List<Site>> listSites() {
		return new HibernateListModel<Site>(Site.class, new BasicCacheableCriteriaBuilder(Order.asc("name")));
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#listSitesPageable()
	 */
	@Override
	public IDataProvider<Site> listSitesPageable() {
		return new HibernateProvider<Site>(Site.class, new BasicCacheableCriteriaBuilder(Order.asc("name")));
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#getSiteById(java.lang.Long)
	 */
	@Override
	public IModel<Site> getSiteById(Long id) {
		return new HibernateObjectModel<Site>(Site.class, id);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#getSiteByName(java.lang.String)
	 */
	@Override
	public IModel<Site> getSiteByName (String name) {
		return new HibernateObjectModel<Site>(Site.class,
				new BasicCacheableCriteriaBuilder(Restrictions.eq("name", name)));
	}
	

	// Period specific methods
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#getPeriodClass()
	 */
	@Override
	public Class<? extends Period> getPeriodClass() {
		return Period.class;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#newPeriod()
	 */
	@Override
	public final Period newPeriod() {
		try {
			return getPeriodClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}	
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#newPeriod(String)
	 */
	public IModel<Period> newPeriod(String defaultName) {
		Site newSite = newSite();
		newSite.setName(defaultName);
		newSite.setSiteId(defaultName);
		Period newPeriod = newPeriod();
		newPeriod.setName(defaultName);
		newPeriod.setClassId(defaultName);
		newPeriod.setSite(newSite);
		cwmService.save(newSite);
		cwmService.save(newPeriod);
		cwmService.flushChanges();
		return new HibernateObjectModel<Period>(newPeriod);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#listPeriods()
	 */
	@Override
	public IModel<List<Period>> listPeriods() {
		return new HibernateListModel<Period>(Period.class, new BasicCriteriaBuilder(Order.asc("name")));
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#getPeriodById(long)
	 */
	@Override
	public IModel<Period> getPeriodById(long id) {
		return new HibernateObjectModel<Period>(Period.class, id);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#getPeriodEditForm(java.lang.String, org.apache.wicket.model.IModel, org.apache.wicket.model.IModel)
	 */
	@Override
	public <T extends Period> Form<T> getPeriodEditForm(String id, Class<T> periodType,
														IModel<Site> site, IModel<Period> mPeriod) {
		// New Period
		if (mPeriod == null || mPeriod.getObject() == null)
			return new HibernateEditPeriodForm<T>(id, site, periodType);
		// Existing Period
		if (!(mPeriod instanceof HibernateObjectModel))
			throw new IllegalArgumentException("This Service class expects HibernateObjectModel)");
		return new HibernateEditPeriodForm<T>(id, (HibernateObjectModel<T>) mPeriod);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#getPeriodEditForm(java.lang.String, org.apache.wicket.model.IModel)
	 */
	@Override
	public <T extends Period> Form<T> getPeriodEditForm(String id, Class<T> periodType, IModel<Site> site) {
		return getPeriodEditForm(id, periodType, site, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#deletePeriod(org.apache.wicket.model.IModel)
	 */
	@Override
	public void deletePeriod(IModel<Period> period) {
		
		cwmService.confirmDatastoreModel(period);
		
		Period p = period.getObject();
		p.getSite().getPeriods().remove(p);
		for (User u : p.getUsers())
			u.getPeriods().remove(p);
		Databinder.getHibernateSession().delete(p);
		
		cwmService.flushChanges();
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.ISiteService#getPeriodByName(java.lang.String)
	 */
	@Override
	public IModel<Period> getPeriodByName(String name) {
		PeriodCriteriaBuilder pcb = new PeriodCriteriaBuilder();
		pcb.setName(name);
		pcb.setMaxResults(1);
		return new HibernateObjectModel<Period>(Period.class, pcb);
	}

	@Override
	public void onPeriodCreated(IModel<? extends Period> mPeriod) {
		// Does nothing by default
	}

	@Override
	public void onPeriodEdited(IModel<? extends Period> mPeriod) {
		// Does nothing by default
	}
}