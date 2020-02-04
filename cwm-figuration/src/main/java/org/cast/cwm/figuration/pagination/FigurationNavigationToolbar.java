/*
 * Copyright 2011-2020 CAST, Inc.
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
package org.cast.cwm.figuration.pagination;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * Variant of {@link }NavigationToolbar} that uses Figuration markup.
 *
 * TODO: Use flex utilities to do better alignment of label vs. navigator
 * TODO: are titles sufficient for a11y, or should we include sr-only text as well on iconic labels?
 * TODO: There are a lot of things that could make this more configurable:
 *   control over whether to include the prev/next and first/last links
 *   control over the CSS classes used
 *   control over the positioning
 *   control over the labels and aria labels.
 *
 * @author bgoldowsky
 */
public class FigurationNavigationToolbar extends NavigationToolbar {

	/**
	 * Constructor
	 *
	 * @param table data table this toolbar will be attached to
	 */
	public FigurationNavigationToolbar(DataTable<?, ?> table) {
		super(table);
	}

	@Override
	protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
		return new FigurationPagingNavigator(navigatorId, table);
	}

}
