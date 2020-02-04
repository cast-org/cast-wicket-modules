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
package org.cast.cwm.admin;

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.figuration.hideable.ConfirmationModal;
import org.cast.cwm.figuration.hideable.FigurationTriggerBehavior;
import org.cast.cwm.service.IAdminPageService;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.ISiteService;
import org.cwm.db.service.IModelProvider;

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
	
	@Inject
	private ISiteService siteService;

	@Inject
	private IAdminPageService adminPageService;

	@Inject
	private IModelProvider modelProvider;


	public SiteListPage(PageParameters parameters) {
		super(parameters);
		
		IDataProvider<Site> siteProvider = siteService.listSitesPageable();
		
		// If no sites in datastore, jump directly to creating a site
		if (siteProvider.size() == 0) {
			setResponsePage(adminPageService.getSiteEditPage());
			return;
		}
		
		// A list of Site objects for the application
		siteList = new DataView<Site>("siteList", siteProvider) {

			@Override
			protected void populateItem(Item<Site> item) {
				
				// Link to edit site directly
				item.add(adminPageService.getSiteEditPageLink("siteLink", item.getModel())
						.add(new Label("name", item.getModelObject().getName())));

				ConfirmationModal<Site> deleteDialog
						= new ConfirmationModal<Site>("deleteSiteModal", item.getModel()) {

					@Override
					protected boolean onConfirm(AjaxRequestTarget target) {
						cwmService.delete(getModel());
						target.add(SiteListPage.this);
						return true;
					}
				};
				item.add(deleteDialog);

				item.add(new WebMarkupContainer("deleteSiteLink").add(new FigurationTriggerBehavior(deleteDialog)));

				// List of periods for the site
				RepeatingView rv = new RepeatingView("periodList");
				item.add(rv);
				for (Period p : item.getModelObject().getPeriods()) {
					WebMarkupContainer c = new WebMarkupContainer(rv.newChildId());
					rv.add(c);
					
					c.add(adminPageService.getPeriodEditPageLink("periodLink", modelProvider.modelOf(p))
							.add(new Label("name", p.getName())));
				}
				
				// Link to add a new period to the site directly
				item.add(adminPageService.getNewPeriodEditPageLink("newPeriodLink", item.getModel()));
			}
		};
		
		siteList.setOutputMarkupId(true);
		add(siteList);
		
		// Link to add a new site
		add(adminPageService.getNewSiteEditPageLink("newSiteLink"));
	}
}
