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
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.devutils.inspector.InspectorPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Role;
import org.cast.cwm.service.IAdminPageService;
import org.cast.cwm.service.ICwmSessionService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is the home page for the RESEARCHER and ADMIN users.
 *
 */
@AuthorizeInstantiation("RESEARCHER")
public class AdminHome extends AdminPage {

	@Inject
	private IAdminPageService adminPageService;

	@Inject
	private ICwmSessionService cwmSessionService;

	public AdminHome(PageParameters parameters) {
		super(parameters);
		setPageTitle("Admin Home");
		RepeatingView linkRepeater = new RepeatingView("category");
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
		for (Entry<String, Map<String, Class<? extends Page>>> cat : getHomePageLinkMap().entrySet()) {
			WebMarkupContainer container = new WebMarkupContainer(repeater.newChildId());
			repeater.add(container);

			container.add(new Label("header", cat.getKey()));

			RepeatingView linkRepeater = new RepeatingView("linkRepeater");
			container.add(linkRepeater);
			for (Entry<String, Class<? extends Page>> link : cat.getValue().entrySet()) {
				WebMarkupContainer linkContainer = new WebMarkupContainer(linkRepeater.newChildId());
				linkRepeater.add(linkContainer);
				linkContainer.add(new BookmarkablePageLink<Void>("link", link.getValue())
					.setBody(Model.of(link.getKey())));
			}
		}
	}
	
	/**
	 * Return a map of components to be added to the list on the home page.
	 * Maps from category names (presented as headers) to submaps where they
	 * key is the link text and the value is the Page class to link to.
	 *
	 * @return Map of category - link - page class
	 */
	protected LinkMap getHomePageLinkMap() {

		LinkMap map = new LinkMap();

		map.addToCategory(Role.ADMIN, "Accounts",
				"Create/Edit Users", adminPageService.getUserListPage());
		map.addToCategory(Role.ADMIN, "Accounts",
				"Create/Edit Sites", adminPageService.getSiteListPage());
		map.addToCategory(Role.ADMIN, "Accounts",
				"Bulk Update Users", adminPageService.getBulkUpdatePage());

		map.addToCategory(Role.RESEARCHER, "Data Analysis",
			"Event Log", EventLogPage.class);
		map.addToCategory(Role.RESEARCHER, "Data Analysis",
				"Event Log Documentation", EventLogDocumentationPage.class);
		map.addToCategory(Role.RESEARCHER, "Data Analysis",
			"User Content Log", UserContentLogPage.class);

		map.addToCategory(Role.ADMIN, "System",
				"Database Statistics", DatabaseStatisticsPage.class);
		map.addToCategory(Role.ADMIN, "System",
				"Cache Management", CacheManagementPage.class);
		map.addToCategory(Role.ADMIN, "System",
				"Open login sessions", SessionListPage.class);
		map.addToCategory(Role.ADMIN, "System",
				"Wicket Inspector Page", InspectorPage.class);

		return map;
	}


	public class LinkMap extends ListOrderedMap<String, Map<String, Class<? extends Page>>> {

		public LinkMap addCategory(String category) {
			if (!containsKey(category))
				put(category, new LinkedHashMap<String, Class<? extends Page>>());
			return this;
		}

		public LinkMap addToCategory(Role role, String category, String linkName, Class<? extends Page> page) {
			if (cwmSessionService.getUser().hasRole(role)) {
				addCategory(category);
				get(category).put(linkName, page);
			}
			return this;
		}

		public LinkMap setCategoryPosition(String category, int position) {
			put(position, category, remove(category));
			return this;
		}

	}
}
