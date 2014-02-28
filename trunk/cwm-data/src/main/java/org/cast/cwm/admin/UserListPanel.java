/*
 * Copyright 2011-2014 CAST, Inc.
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

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.builders.UserCriteriaBuilder;
import org.cast.cwm.service.UserService;

/**
 * This panel creates a table of users.  Each row has an edit user link.
 *
 * TODO: Look into {@link UserService#getUserListProvider}.
 */
public class UserListPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private UserCriteriaBuilder builder;

	public UserListPanel(String id) {
		super(id);
		ISortableDataProvider<User,String> provider = getDataProvider(getCriteriaBuilder());
		DefaultDataTable<User,String> table = new DefaultDataTable<User,String>("userList", makeColumns(), provider, 25);
		table.addBottomToolbar(new NavigationToolbar(table));
		add(table);
	}
	
	protected UserCriteriaBuilder getCriteriaBuilder() {
		if (builder == null) 
			builder = new UserCriteriaBuilder();
		return builder;
	}
	
	protected ISortableDataProvider<User,String> getDataProvider(UserCriteriaBuilder builder) {
		SortableHibernateProvider<User> provider = new SortableHibernateProvider<User>(User.class, builder);
		provider.setWrapWithPropertyModel(false);
		return provider;
	}
	
	public UserListPanel filterByRole(Role r) {
		getCriteriaBuilder().setRole(r);
		return this;
	}
	
	public UserListPanel filterByPeriod(IModel<Period> period) {
		getCriteriaBuilder().setPeriod(period);
		return this;
	}
	
	protected List<IColumn<User,String>> makeColumns() {
		List<IColumn<User,String>> columns = new ArrayList<IColumn<User,String>>();
				
		columns.add(new PropertyColumn<User,String>(new Model<String>("User Name"), "username", "username"));
		columns.add(new PropertyColumn<User,String>(new Model<String>("First Name"), "firstName", "firstName"));
		columns.add(new PropertyColumn<User,String>(new Model<String>("Last Name"), "lastName", "lastName"));
		columns.add(new PropertyColumn<User,String>(new Model<String>("Role"), "role", "role"));
		columns.add(new PropertyColumn<User,String>(new Model<String>("Permission"), "permission", "permission"));
		
		columns.add(new AbstractColumn<User,String>(new Model<String>("Edit")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				cellItem.add(new EditLinkFragment(componentId, rowModel));
			}
		});

		return columns;
	}

	/**
	 * A simple link to edit a user.  Since {@link IColumn} objects don't play nicely
	 * with anything but labels.
	 * 
	 * @author jbrookover
	 *
	 */
	public class EditLinkFragment extends Fragment {

		private static final long serialVersionUID = 1L;

		public EditLinkFragment(String id, IModel<User> model) {
			super(id, "editLinkFragment", UserListPanel.this, model);
			PageParameters pp = new PageParameters();
			pp.set("userId", model.getObject().getId());
			add(new BookmarkablePageLink<Void>("link", UserFormPage.class, pp));
		}
	}
	
	@Override
	protected void onDetach() {
		builder.detach();
		super.onDetach();
	}
}
