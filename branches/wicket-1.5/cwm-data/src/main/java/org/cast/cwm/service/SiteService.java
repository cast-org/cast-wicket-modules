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
package org.cast.cwm.service;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateListModel;
import net.databinder.models.hib.HibernateObjectModel;
import net.databinder.models.hib.HibernateProvider;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.builders.CachingCriteriaBuilder;
import org.cast.cwm.data.builders.PeriodCriteriaBuilder;
import org.cast.cwm.data.component.HibernateEditPeriodForm;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Inject;

/**
 * General Service Class for both Sites and Periods  
 */
public class SiteService {

	protected static SiteService instance = new SiteService();

	@Inject
	private ICwmService cwmService;

	@Getter @Setter
	private Class<? extends Period> periodClass = Period.class;
	
	@Getter @Setter
	private Class<? extends Site> siteClass = Site.class;

	public SiteService() {
		Injector.get().inject(this);
	}
	
	public static SiteService get() {
		return instance;
	}

	public static void setInstance(SiteService instance) {
		SiteService.instance = instance;
	}
	
	
	// Site specific methods
	
	/**
	 * Create a new instance of the specified Site class.
	 * @return
	 */
	public final Site newSite() {
		try {
			return siteClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}	
	}

	public IModel<List<Site>> listSites() {
		return new HibernateListModel<Site>(Site.class, new CachingCriteriaBuilder());
	}
	
	public IDataProvider<Site> listSitesPageable() {
		return new HibernateProvider<Site>(Site.class, new CachingCriteriaBuilder());
	}

	public IModel<Site> getSiteById(Long id) {
		return new HibernateObjectModel<Site>(Site.class, id);
	}
	
	public IModel<Site> getSiteByName (String name) {
		Criteria criteria = Databinder.getHibernateSession().createCriteria(Site.class);
		criteria.add(Restrictions.eq("name", name));
		criteria.setCacheable(true);
		return new HibernateObjectModel<Site>((Site)criteria.uniqueResult());
	}
	

	// Period specific methods
	
	/**
	 * Create a new instance of the specified Period class.
	 * @return
	 */
	public final Period newPeriod() {
		try {
			return periodClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}	
	}

	public IModel<List<Period>> listPeriods() {
		return new HibernateListModel<Period>(Period.class);
	}
	
	public IModel<Period> getPeriodById(long id) {
		return new HibernateObjectModel<Period>(Period.class, id);
	}
	
	public Form<Period> getPeriodEditForm(String id, IModel<Site> site, IModel<Period> period) {
		// New Period
		if (period == null || period.getObject() == null)
			return new HibernateEditPeriodForm(id, site, periodClass);
		// Existing Period
		if (!(period instanceof HibernateObjectModel))
			throw new IllegalArgumentException("This Service class expects a UserModel (which extends Hibernate)");
		return new HibernateEditPeriodForm(id, (HibernateObjectModel<Period>) period);
	}
	
	public Form<Period> getPeriodEditForm(String id, IModel<Site> site) {
		return getPeriodEditForm(id, site, null);
	}
	
	/**
	 * Deletes a period from the datastore, removing all associations
	 * with related objects (e.g. {@link User}s and {@link Site}).
	 * 
	 * @param period
	 */
	public void deletePeriod(IModel<Period> period) {
		
		cwmService.confirmDatastoreModel(period);
		
		Period p = period.getObject();
		p.getSite().getPeriods().remove(p);
		for (User u : p.getUsers())
			u.getPeriods().remove(p);
		Databinder.getHibernateSession().delete(p);
		
		cwmService.flushChanges();
	}

	/**
	 * Get the period by name - this assumes the name is unique
	 */
	public IModel<Period> getPeriodByName(String name) {
		PeriodCriteriaBuilder pcb = new PeriodCriteriaBuilder();
		pcb.setName(name);
		pcb.setMaxResults(1);
		return new HibernateObjectModel<Period>(periodClass, pcb);
	}

}