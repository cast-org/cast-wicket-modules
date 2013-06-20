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
package org.cast.cwm.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.component.DeletePersistedObjectDialog;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.SiteService;

import com.google.inject.Inject;

/**
 * Page for viewing sites and their associated periods.  Links generated for edit site, edit
 * period or creating a new site.
 *
 */
@AuthorizeInstantiation("ADMIN")
public class SiteListPage extends AdminPage {

	private DataView<Site> siteList;
	
	@Inject
	private ICwmService cwmService;

	private static final long serialVersionUID = 1L;

	public SiteListPage(PageParameters parameters) {
		super(parameters);
		
		IDataProvider<Site> siteProvider = SiteService.get().listSitesPageable();
		
		// If no sites in datastore, jump directly to creating a site
		if (siteProvider.size() == 0) {
			setResponsePage(SiteInfoPage.class);
			return;
		}
		
		// A list of Site objects for the application
		siteList = new DataView<Site>("siteList", siteProvider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<Site> item) {
				
				// Link to edit site directly
				item.add(new BookmarkablePageLink<Void>("siteLink", SiteInfoPage.class,
						new PageParameters().set("siteId", item.getModelObject().getId()))
						 	.add(new Label("name", item.getModelObject().getName())));
				
				DeletePersistedObjectDialog<Site> dialog = new DeletePersistedObjectDialog<Site>("deleteSiteModal", item.getModel()) {
					
					private static final long serialVersionUID = 1L;

					@Override
					protected void deleteObject() {
						cwmService.delete(getModel());
					}
				};
				item.add(dialog);
				item.add(new WebMarkupContainer("deleteSiteLink").add(dialog.getDialogBorder().getClickToOpenBehavior()));

				// List of periods for the site
				RepeatingView rv = new RepeatingView("periodList");
				item.add(rv);
				for (Period p : item.getModelObject().getPeriods()) {
					WebMarkupContainer c = new WebMarkupContainer(rv.newChildId());
					rv.add(c);
					
					c.add(new BookmarkablePageLink<Void>("periodLink", PeriodInfoPage.class,
							new PageParameters().set("periodId", p.getId()))
								.add(new Label("name", p.getName())));
				}
				
				// Link to add a new period to the site directly
				item.add(new BookmarkablePageLink<Void>("newPeriodLink", PeriodInfoPage.class,
						new PageParameters().set("siteId", item.getModelObject().getId())));
			}
		};
		
		siteList.setOutputMarkupId(true);
		add(siteList);
		
		// Link to add a new site
		add(new BookmarkablePageLink<Void>("newSiteLink", SiteInfoPage.class));
	}
}
