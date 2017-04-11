/*
 * Copyright 2011-2017 CAST, Inc.
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

import java.util.Arrays;

import com.google.inject.Inject;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Role;
import org.cast.cwm.service.IAdminPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page enables the viewing of users in a table using {link @UserListPanel}.
 * Links are created to add users for each {link @Role}.
 *
 */
@AuthorizeInstantiation("ADMIN")
public class UserListPage extends AdminPage {

	@Inject
	private IAdminPageService adminPageService;

	public UserListPage(final PageParameters parameters) {
		super(parameters);
		
		setPageTitle("User List");
		
		UserListPanel list = new UserListPanel("userList");
		add(list);
		
		ListView<Role> addUserLinks = new ListView<Role>("addUserList", Arrays.asList(Role.values())) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Role> item) {
				PageParameters pp = new PageParameters();
				pp.set("role", item.getModelObject().getRoleString());
				item.add(adminPageService.getNewUserEditPageLink("addLink", item.getModelObject(), null)
						.add(new Label("role", item.getModelObject().toString())));
				if (item.getIndex() != 0)
					item.add(AttributeModifier.replace("class", "addSeparator"));
			}
		};
		
		add(addUserLinks);		
	}
}
