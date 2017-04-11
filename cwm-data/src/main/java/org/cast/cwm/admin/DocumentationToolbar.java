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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author bgoldowsky
 */
public class DocumentationToolbar extends AbstractToolbar {

	public <T,S> DocumentationToolbar(final DataTable<T, S> table) {
		super(table);

		RefreshingView<IColumn<T, S>> docs = new RefreshingView<IColumn<T, S>>("docs") {

			@Override
			protected Iterator<IModel<IColumn<T, S>>> getItemModels() {
				List<IModel<IColumn<T, S>>> columnsModels = new LinkedList<IModel<IColumn<T, S>>>();

				for (IColumn<T, S> column : table.getColumns()) {
					columnsModels.add(Model.of(column));
				}

				return columnsModels.iterator();
			}

			@Override
			protected void populateItem(Item<IColumn<T, S>> item) {
				final IColumn<T, S> column = item.getModelObject();
				Component header;
				if (column instanceof IDocumentedColumn) {
					header = new Label("documentation", ((IDocumentedColumn) column).getDocumentationModel());
				} else {
					header = new WebMarkupContainer("documentation");
				}
				item.add(header);
			}

		};
		add(docs);
	}

}
