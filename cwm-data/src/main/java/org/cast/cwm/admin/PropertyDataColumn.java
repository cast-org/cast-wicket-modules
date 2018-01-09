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

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Extension of PropertyColumn that can also function as an IDataColumn for static download.
 * The header should be specified as a simple string.  The cell values will be extracted
 * based on the property expression, and converted to a string.
 *
 * A documentation model, or literal string, can also be set for use with
 * {@link org.cast.cwm.admin.DocumentationToolbar}.
 * 
 * @author bgoldowsky
 *
 */
public class PropertyDataColumn<E> extends PropertyColumn<E,String>
		implements IDataColumn<E>, IDocumentedColumn {

	private static final long serialVersionUID = 1L;

	@Getter @Setter
	private IModel<String> documentationModel = null;

	public PropertyDataColumn(String headerString, String propertyExpression) {
		super(new Model<String>(headerString), propertyExpression);
	}
	
	public PropertyDataColumn(String headerString, String sortProperty,
			String propertyExpression) {
		super(new Model<String>(headerString), sortProperty, propertyExpression);
	}

	@Override
	public String getHeaderString() {
		return getDisplayModel().getObject();
	}

	@Override
	public String getItemString(IModel<E> rowModel) {
		Object rowObj = getDataModel(rowModel).getObject();
		return rowObj==null ? "" : rowObj.toString();
	}

	public String getDocumentation() {
		if (documentationModel != null)
			return documentationModel.getObject();
		return null;
	}

	public void setDocumentation(String documentation) {
		documentationModel = Model.of(documentation);
	}

	@Override
	public void detach() {
		super.detach();
		if (documentationModel != null)
			documentationModel.detach();
	}

}
