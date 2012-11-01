/*
 * Copyright 2011 CAST, Inc.
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

import java.util.ArrayList;
import java.util.List;

import net.databinder.models.hib.SortableHibernateProvider;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.data.builders.LoginSessionCriteriaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page enables the viewing of users in a table using {link @UserListPanel}.
 * Links are created to add users for each {link @Role}.
 *
 */
@AuthorizeInstantiation("ADMIN")
public class SessionListPage extends AdminPage {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SessionListPage.class);

	public SessionListPage(final PageParameters parameters) {
		super(parameters);
		
		setPageTitle("Session List");
		
		LoginSessionCriteriaBuilder builder = new LoginSessionCriteriaBuilder();
		SortableHibernateProvider<LoginSession> provider = new SortableHibernateProvider<LoginSession>(LoginSession.class, builder);
		provider.setWrapWithPropertyModel(false);
		DefaultDataTable<LoginSession> table = new DefaultDataTable<LoginSession>("sessionList", makeColumns(), provider, 25);
		table.addBottomToolbar(new NavigationToolbar(table));
		add(table);		
	}
	
	protected List<IColumn<LoginSession>> makeColumns() {
		List<IColumn<LoginSession>> columns = new ArrayList<IColumn<LoginSession>>();
	
		columns.add(new PropertyColumn<LoginSession>(new Model<String>("User Name"), "user.username", "user.username"));
		columns.add(new PropertyColumn<LoginSession>(new Model<String>("Start time"), "startTime", "startTime"));
		// FIXME the getSecondsSinceLastEvent() method doesn't currently exist.
		columns.add(new PropertyColumn<LoginSession>(new Model<String>("Time since last event"), "secondsSinceLastEvent")); // cannot sort since it's not a field Hibernate knows about
		columns.add(new PropertyColumn<LoginSession>(new Model<String>("Browser"), "userAgent"));
		columns.add(new PropertyColumn<LoginSession>(new Model<String>("IP Address"), "ipAddress"));
	
		return columns;
	}
	
}
