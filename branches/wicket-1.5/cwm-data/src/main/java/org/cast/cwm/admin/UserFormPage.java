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

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.service.SiteService;
import org.cast.cwm.service.UserService;
import org.cast.cwm.service.UserService.LoginData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page for adding a new or editing existing user.
 *
 */
@AuthorizeInstantiation("ADMIN")
public class UserFormPage extends AdminPage {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(UserFormPage.class);
	private HibernateObjectModel<User> userModel;
	private IModel<Period> periodModel = new Model<Period>(null);
	private Role role = null;

	private static final long serialVersionUID = 1L;

	public UserFormPage(final PageParameters parameters) {
		super(parameters);
		
		// The user we're editing, if any
		if (!parameters.get("userId").isEmpty())
			userModel = (HibernateObjectModel<User>) UserService.get().getById(parameters.get("userId").toLongObject());
		// The role of the user to create, if any
		if (!parameters.get("role").isEmpty())
			role = Role.forRoleString(parameters.get("role").toString());
		// The period to link back to, if any
		if (!parameters.get("periodId").isEmpty())
			periodModel = SiteService.get().getPeriodById(parameters.get("periodId").toLongObject());
		
		addBreadcrumbLinks();
		
		// Edit an existing user
		if (userModel!= null && userModel.getObject() != null) {
			add(new EditUserPanel("editUserPanel", userModel));
			
		// Edit a new user, setting the Role and Default Period, if necessary
		} else if (role != null) {
			EditUserPanel p = new EditUserPanel("editUserPanel");
			p.getUserModel().getObject().setRole(role);
			p.setEnabled("role", false);
			p.setAutoConfirmNewUser(true);  // Creating via admin shouldn't require confirmation
			if (periodModel.getObject() != null)
				p.getUserModel().getObject().getPeriods().add(periodModel.getObject());
			add(p);
			
		// For now, we only create new users if we know their role.
		} else {
			throw new IllegalStateException("Cannot Edit User without existing user or default role.");
		}
		
		addLoginHistory();
	}

	// TODO: This is a bit overkill
	protected void addBreadcrumbLinks() {
		
		RepeatingView repeater = new RepeatingView("breadCrumbLinkRepeater");
		
		// Breadcrumbs back to Period / Site
		if (periodModel.getObject() != null) {
			
			WebMarkupContainer site = new WebMarkupContainer(repeater.newChildId());
			WebMarkupContainer period = new WebMarkupContainer(repeater.newChildId());
			repeater.add(site);
			repeater.add(period);
			
			site.add(new BookmarkablePageLink<Void>("link", SiteInfoPage.class)
					.setParameter("siteId", periodModel.getObject().getSite().getId())
					.add(new Label("label", periodModel.getObject().getSite().getName())));

			period.add(new BookmarkablePageLink<Void>("link", PeriodInfoPage.class)
					.setParameter("periodId", periodModel.getObject().getId())
					.add(new Label("label", periodModel.getObject().getName())));
			period.add(new SimpleAttributeModifier("class", "addSeparator"));

		// Breadcrumb link back to List of Users
		}  else {
			WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
			repeater.add(item);
			
			BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", UserListPage.class);
			link.add(new Label("label", "User List"));
			item.add(link);
		}
		
		add(repeater);
	}
	
	protected void addLoginHistory() {
		LoginData data = null;
		if  (userModel != null && userModel.getObject() != null && !userModel.getObject().isTransient()) {
			data = UserService.get().getLoginSessions(userModel); 
		}
		if (data != null) {
			add(new Label("logincount", data.getLoginCount().toString()));
			add(new Label("logindate", data.getLastLogin() == null ? "Never" : data.getLastLogin().toString()));
		} else {
			add(new Label("logincount", "0").setVisible(false));
			add(new Label("logindate", "never"));		
		}
	}
	
	@Override
	protected void onDetach() {
		periodModel.detach();
		super.onDetach();
	}
	
}