/*
 * Copyright 2011-2018 CAST, Inc.
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

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Default implementation of IDataColumn.
 * Expects the column header to be a simple string.
 * Implementations can define a getItemString() method, and this class will by default return
 * that string as a Label object for use in DataTables.
 * 
 * @author bgoldowsky
 *
 */
public abstract class AbstractDataColumn<E> extends AbstractColumn<E,String> implements IDataColumn<E> {

	private static final long serialVersionUID = 1L;
	
	public AbstractDataColumn(String headerString) {
		super(new Model<String>(headerString));
	}
	
	public AbstractDataColumn(String headerString, String sortProperty) {
		super(new Model<String>(headerString), sortProperty);
	}

	@Override
	public String getHeaderString() {
		return getDisplayModel().getObject();
	}
	
	// Default implementation
	@Override
	public void populateItem(Item<ICellPopulator<E>> cellItem, String componentId, IModel<E> rowModel) {
		cellItem.add(new Label(componentId, getItemString(rowModel)));
	}

}
