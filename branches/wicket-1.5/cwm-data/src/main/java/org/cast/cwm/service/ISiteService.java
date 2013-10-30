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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;

public interface ISiteService {

	public Class<? extends Site> getSiteClass();

	/**
	 * Create a new instance of the specified Site class.
	 * @return
	 */
	public Site newSite();

	public IModel<List<Site>> listSites();

	public IDataProvider<Site> listSitesPageable();

	public IModel<Site> getSiteById(Long id);

	public IModel<Site> getSiteByName(String name);

	public Class<? extends Period> getPeriodClass();

	/**
	 * Create a new instance of the specified Period class.
	 * @return
	 */
	public Period newPeriod();

	public IModel<List<Period>> listPeriods();

	public IModel<Period> getPeriodById(long id);

	public Form<Period> getPeriodEditForm(String id, IModel<Site> site,
			IModel<Period> period);

	public Form<Period> getPeriodEditForm(String id, IModel<Site> site);

	/**
	 * Deletes a period from the datastore, removing all associations
	 * with related objects (e.g. {@link User}s and {@link Site}).
	 * 
	 * @param period
	 */
	public void deletePeriod(IModel<Period> period);

	/**
	 * Get the period by name - this assumes the name is unique
	 */
	public IModel<Period> getPeriodByName(String name);

}