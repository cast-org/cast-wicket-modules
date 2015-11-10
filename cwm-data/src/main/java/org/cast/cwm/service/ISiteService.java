/*
 * Copyright 2011-2014 CAST, Inc.
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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;

import java.util.List;

public interface ISiteService {

	Class<? extends Site> getSiteClass();

	/**
	 * Create a new instance of the specified Site class.
	 * @return the new Site
	 */
	Site newSite();

	IModel<List<Site>> listSites();

	IDataProvider<Site> listSitesPageable();

	IModel<Site> getSiteById(Long id);

	IModel<Site> getSiteByName(String name);

	Class<? extends Period> getPeriodClass();

	/**
	 * Create a new instance of the specified Period class.
	 * @return the new Period
	 */
	Period newPeriod();

	IModel<List<Period>> listPeriods();

	IModel<Period> getPeriodById(long id);

	<T extends Period> Form<T> getPeriodEditForm(String id, Class<T> periodClass,
												 IModel<Site> site, IModel<Period> period);

	<T extends Period> Form<T> getPeriodEditForm(String id, Class<T> periodClass,
													  IModel<Site> site);

	/**
	 * Deletes a period from the datastore, removing all associations
	 * with related objects (e.g. {@link User}s and {@link Site}).
	 * 
	 * @param period model of the Period to delete
	 */
	void deletePeriod(IModel<Period> period);

	/**
	 * Get the period by name - this assumes the name is unique
	 */
	IModel<Period> getPeriodByName(String name);

	/**
	 * Create a new Site and Period within that site with a given name.
	 * @param defaultName
	 * @return
	 */
	IModel<Period> newPeriod(String defaultName);

	/**
	 * Override to take action whenever a new Period is created by administrative action.
	 * Does nothing by default.
	 *
	 * @param mPeriod model of the new Period.
	 */
	void onPeriodCreated(IModel<? extends Period> mPeriod);

	/**
	 * Override to take action whenever a Period is edited by administrative action.
	 * Does nothing by default.
	 *
	 * @param mPeriod model of the edited Period.
	 */
	void onPeriodEdited(IModel<? extends Period> mPeriod);

}