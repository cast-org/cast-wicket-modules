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

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.devutils.inspector.InspectorPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Role;

/**
 * This is the home page for the ADMIN users.
 *
 */
@AuthorizeInstantiation("RESEARCHER")
public class AdminHome extends AdminPage {

	private static final long serialVersionUID = 1L;

	public AdminHome(PageParameters parameters) {
		super(parameters);
		setPageTitle("Admin Home");
		RepeatingView linkRepeater = new RepeatingView("linkRepeater");
		add(linkRepeater);
		addLinks(linkRepeater);
	}
	
	/**
	 * Add the links to the Admin Home Page.  Override this 
	 * method - and call super() - to add additional links.
	 * 
	 * Each repeater item has a "link" with a "label".
	 */
	protected void addLinks(RepeatingView repeater) {
		for (Component c : homePageComponents()) {
			WebMarkupContainer container = new WebMarkupContainer(repeater.newChildId());
			container.add(c);
			repeater.add(container);
		}
	}
	
	/**
	 * Return a list of components to be added to the list on the home page.
	 * This is usually a list of links.
	 * @return 
	 */
	protected List<Component> homePageComponents() {
		LinkedList<Component> list = new LinkedList<Component>(); 
			
		// Links for users with full admin rights
		if (CwmSession.get().getUser().hasRole(Role.ADMIN)) {
			list.add(new BookmarkablePageLink<Page>("link", UserListPage.class).add(new Label("label", "Create/Edit Users")));
			list.add(new BookmarkablePageLink<Page>("link", SiteListPage.class).add(new Label("label", "Create/Edit Sites")));
			list.add(new BookmarkablePageLink<Page>("link", DatabaseStatisticsPage.class).add(new Label("label", "Database Statistics")));
			list.add(new BookmarkablePageLink<Page>("link", SessionListPage.class).add(new Label("label", "Open login sessions")));
			list.add(new BookmarkablePageLink<Page>("link", InspectorPage.class).add(new Label("label", "Wicket Inspector Page")));
			// Can't just use a bookmarkable page link for data browser, since it's only bookmarkable in development mode
//			list.add(new Link<Void>("link") {
//				private static final long serialVersionUID = 1L;
//				@Override
//				public void onClick() {
//					setResponsePage(new net.databinder.components.hib.DataBrowser<Void>(true));					
//				}
//			}.add(new Label("label", "Data browser")));
		}
		
		list.add(new BookmarkablePageLink<Page>("link", EventLog.class).add(new Label("label", "Event Log")));
		
		return list;
	}

}
