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
package org.cast.cwm.xml.component;

import java.util.List;

import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.xml.XmlSection;

public abstract class ChildListView extends ListView<XmlSection> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a ChildListView showing each child of the given section.
	 * The model parameter should be an XmlSectionModel of the parent.
	 * @param id
	 * @param model
	 */
	public ChildListView(String id, IModel<XmlSection> model) {
		super(id, new PropertyModel<List<XmlSection>>(model, "children"));
	}

}