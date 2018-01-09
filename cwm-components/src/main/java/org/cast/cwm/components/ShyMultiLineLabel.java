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
package org.cast.cwm.components;

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

/**
 * A MultiLineLabel that makes itself invisible whenever it has nothing to say.
 * This is convenient if, say, it is inside a <wicket:enclosure> that 
 * should be hidden when the label content is empty.
 *
 */
public class ShyMultiLineLabel extends MultiLineLabel {

	private static final long serialVersionUID = 1L;

	public ShyMultiLineLabel(String id, IModel<?> model) {
		super(id, model);
	}

	public ShyMultiLineLabel(String id, String label) {
		super(id, label);
	}

	public ShyMultiLineLabel(String id) {
		super(id);
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!Strings.isEmpty(getDefaultModelObjectAsString()));
	}

}
