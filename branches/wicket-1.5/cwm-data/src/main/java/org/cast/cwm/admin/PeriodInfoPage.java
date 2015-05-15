/*
 * Copyright 2011-2015 CAST, Inc.
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
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.service.ISiteService;

import com.google.inject.Inject;

@AuthorizeInstantiation("ADMIN")
public class PeriodInfoPage extends AdminPage {
	
	@Inject
	protected ISiteService siteService;

	private IModel<Period> period = new Model<Period>(null);
	private IModel<Site> site = new Model<Site>(null);
	
	private static final long serialVersionUID = 1L;

	public PeriodInfoPage(PageParameters parameters) {
		super(parameters);
		
		// Get Period, or Site used to create a new period.  Otherwise, redirect
		if (!parameters.get("periodId").isEmpty()) {
			period = siteService.getPeriodById(parameters.get("periodId").toLongObject());
			site = siteService.getSiteById(period.getObject().getSite().getId());
		} else if (!parameters.get("siteId").isEmpty()) {
			site = siteService.getSiteById(parameters.get("siteId").toLongObject());
		} else {
			setResponsePage(SiteListPage.class);
			return;
		}
		
		BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("siteLink", SiteInfoPage.class);
		link.getPageParameters().set("siteId", site.getObject().getId());
		add(link);
		link.add(new Label("name", site.getObject().getName()));		

		if (period.getObject() == null) {
			add(new Label("instructions", "Create New Period"));
			add(siteService.getPeriodEditForm("form", site));
			// TODO: Cleaner way to do this?
			add(new WebMarkupContainer("newStudentLink").setVisible(false));
			add(new WebMarkupContainer("newTeacherLink").setVisible(false));
		} else {
			add(new Label("instructions", new AbstractReadOnlyModel<String>() {

				private static final long serialVersionUID = 1L;

				@Override
				public String getObject() {
					return "Edit Period: " + period.getObject().getName();
				}
				
			}));
			add(siteService.getPeriodEditForm("form", site, period));
			
			link = new BookmarkablePageLink<Void>("newStudentLink", UserFormPage.class);
			link.getPageParameters().set("periodId", period.getObject().getId()).set("role", Role.STUDENT_ROLENAME);
			add(link);

			link = new BookmarkablePageLink<Void>("newTeacherLink", UserFormPage.class);
			link.getPageParameters().set("periodId", period.getObject().getId()).set("role", Role.TEACHER_ROLENAME);
			add(link);
		}
		
		UserListPanel list = new UserListPanel("userList");
		list.filterByPeriod(period);
				
		add(list);
	}
	
	@Override
	protected void onDetach() {
		period.detach();
		site.detach();
		super.onDetach();
	}
}
