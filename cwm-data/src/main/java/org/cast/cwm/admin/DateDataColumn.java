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

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.wicketstuff.datetime.markup.html.basic.DateLabel;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A PropertyDataColumn whose content is a date.
 * The date information in the column will be formatted in a spreadsheet-friendly way.
 *
 * @author bgoldowsky
 */
public class DateDataColumn<E> extends PropertyDataColumn<E> {

	@Getter @Setter
	protected String eventDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";

	public DateDataColumn(String headerString, String propertyExpression) {
		super(headerString, propertyExpression);
	}

	public DateDataColumn(String headerString, String sortProperty, String propertyExpression) {
		super(headerString, sortProperty, propertyExpression);
	}

	@Override
	public void populateItem(Item<ICellPopulator<E>> cellItem, String componentId, IModel<E> rowModel) {
		cellItem.add(DateLabel.forDatePattern(componentId, getDataModel(rowModel), eventDateFormat));
	}

	@Override
	public String getItemString(IModel<E> rowModel) {
		Date date = getDataModel(rowModel).getObject();
		if (date != null)
			return new SimpleDateFormat(eventDateFormat).format(date);
		else
			return "";
	}

	@SuppressWarnings("unchecked")
	@Override
	public IModel<Date> getDataModel(IModel<E> rowModel) {
		return (IModel<Date>) super.getDataModel(rowModel);
	}
}
