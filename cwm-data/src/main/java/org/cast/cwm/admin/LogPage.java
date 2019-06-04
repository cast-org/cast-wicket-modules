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
package org.cast.cwm.admin;

import com.google.inject.Inject;
import net.databinder.models.DetachableListModel;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Site;
import org.cast.cwm.db.service.IModelProvider;
import org.cast.cwm.service.ISiteService;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Superclass for pages that display some log data, with filtering options.
 *
 * @author bgoldowsky
 */
public abstract class LogPage extends AdminPage {

	protected static final long ITEMS_PER_PAGE = 50;
	protected int numberOfSites;
	protected IModel<List<Site>> mShowSites;

	protected IModel<Date> mFromDate, mToDate;

	@Inject
	private ISiteService siteService;

	@Inject
	private IModelProvider modelProvider;

	public LogPage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		if (mFromDate != null)
			mFromDate.detach();
		if (mToDate != null)
			mToDate.detach();
		if (mShowSites != null)
			mShowSites.detach();
	}

	protected void addDateFilter(Form<Object> form) {
		DateTime currentDateTime = new DateTime(new Date());
		mToDate = new Model<Date>(currentDateTime.toDate());
		mFromDate = new Model<Date>(currentDateTime.minusMonths(1).toDate());

		form.add(new DateTextField("from", mFromDate));
		form.add(new DateTextField("to", mToDate));
	}

	protected void addSiteFilter(Form<Object> form) {
		List<Site> allSites = siteService.listSites().getObject();
		DetachableListModel<Site> mAllSites = new DetachableListModel<>(modelProvider, allSites);
		List<Site> showSites = new ArrayList<>(allSites);
		mShowSites = new DetachableListModel<>(modelProvider, showSites);
		numberOfSites = showSites.size();
		if (!allSites.isEmpty())
			form.add(new CheckBoxMultipleChoice<>("site", mShowSites, mAllSites,
					new ChoiceRenderer<Site>("name", "id")));
		else
			form.add(new WebMarkupContainer("site").setVisible(false));
	}

}
