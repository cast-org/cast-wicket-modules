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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.devutils.inspector.InspectorPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Role;
import org.cast.cwm.service.ICwmSessionService;

import com.google.inject.Inject;

/**
 * This is the home page for the ADMIN users.
 *
 */
@AuthorizeInstantiation("RESEARCHER")
public class AdminHome extends AdminPage {
	
	@Inject
	ICwmSessionService cwmSessionService;

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
		for (Entry<String, Class<? extends Page>> e : getHomePageLinkMap().entrySet()) {
			WebMarkupContainer container = new WebMarkupContainer(repeater.newChildId());
			BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", e.getValue());
			link.add(new Label("label", e.getKey()));
			container.add(link);
			repeater.add(container);
		}
	}
	
	/**
	 * Return a map of components to be added to the list on the home page.
	 * This is usually a list of links.
	 * @return 
	 */
	protected Map<String,Class<? extends Page>> getHomePageLinkMap() {
		Map<String,Class<? extends Page>> map = new LinkedHashMap<String,Class<? extends Page>>();
			
		// Links for users with full admin rights
		if (cwmSessionService.getUser().hasRole(Role.ADMIN)) {
			map.put("Create/Edit Users", UserListPage.class);
			map.put("Create/Edit Sites", SiteListPage.class);
			map.put("Database Statistics", DatabaseStatisticsPage.class);
			map.put("Cache Management", CacheManagementPage.class);
			map.put("Open login sessions", SessionListPage.class);
			map.put("Wicket Inspector Page", InspectorPage.class);
		}
		
		// Links for admins and researchers
		map.put("Event Log", EventLog.class);
		map.put("User Content Log", UserContentLogPage.class);
		
		return map;
	}

}
