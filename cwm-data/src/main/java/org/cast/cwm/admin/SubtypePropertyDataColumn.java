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

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * DataColumn for a property that only exists in a subtype of the base type.
 * Works the same as @PropertyDataColumn, but will put blank cells in for any
 * data row that is not of the correct subtype.
 * 
 * @param <T> the row type of the data grid
 *  
 * @author bgoldowsky
 *
 */
public class SubtypePropertyDataColumn<T> extends PropertyDataColumn<T> {

	private static final long serialVersionUID = 1L;
	private Class<? extends T> subtype;
	
	public SubtypePropertyDataColumn(String headerString, String propertyExpression, Class<? extends T> subtype) {
		super(headerString, propertyExpression);
		this.subtype = subtype;
	}

	@Override
	public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
		if (subtype.isInstance(rowModel.getObject()))
			super.populateItem(cellItem, componentId, rowModel);
		else
			cellItem.add(new EmptyPanel(componentId));
	}

	@Override
	public String getItemString(IModel<T> rowModel) {
		if (subtype.isInstance(rowModel.getObject()))
			return super.getItemString(rowModel);
		else
			return "";
	}
}