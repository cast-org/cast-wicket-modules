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
package org.cast.cwm.figuration.pagination;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.*;

/**
 * Variant of {@link AjaxPagingNavigator} with Figuration markup.
 * Does not include all features of AjaxPagingNavigator at the moment, just the simple list of page links,
 * not "first page", "last page" etc.
 *
 * @author bgoldowsky
 */
public class FigurationPagingNavigator extends PagingNavigator {

	public FigurationPagingNavigator(String id, IPageable pageable) {
		super(id, pageable);
	}

	@Override
	protected PagingNavigation newNavigation(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
		PagingNavigation pagingNavigation = new PagingNavigation(id, pageable, labelProvider) {
			@Override
			protected Link<?> newPagingNavigationLink(String id, IPageable pageable, long pageIndex) {
				return new PagingNavigationLink(id, pageable, pageIndex) {
					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (getPageNumber() == pageable.getCurrentPage())
							tag.append("class", "active", " ");
					}
				}.setAutoEnable(false);
			}
		};
		return pagingNavigation;
	}

}
