/*
 * Copyright 2011-2016 CAST, Inc.
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

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;

/** 
 * An IColumn that can also be used for static data table download.
 * This requires that header and data item content can be retrieved as simple strings
 * in addition to retrieval as Wicket components.
 * 
 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn
 * 
 * @author bgoldowsky
 * 
 * TODO: allow parameterization of sort field type
 *
 */
public interface IDataColumn<E> extends IColumn<E,String> {

	String getHeaderString();
	
	String getItemString(IModel<E> rowModel);

}
