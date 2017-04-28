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
package org.cast.cwm.figuration.component;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.AbstractRepeater;

/**
 * Variant of {@link AjaxPagingNavigator} with Figuration markup.
 * Does not include all features of AjaxPagingNavigator at the moment, just the simple list of page links,
 * not "first page", "last page" etc.
 *
 * @author bgoldowsky
 */
public class FigurationAjaxPagingNavigator extends Panel {

	private final IPageable pageable;
	private final IPagingLabelProvider labelProvider = null;


	public FigurationAjaxPagingNavigator(String id, IPageable pageable) {
		super(id);
		this.pageable = pageable;
		setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PagingNavigation pagingNavigation = new AjaxPagingNavigation("navigation", pageable, labelProvider) {
			@Override
			protected Link<?> newPagingNavigationLink(String id, IPageable pageable, long pageIndex) {
				return new AjaxPagingNavigationLink(id, pageable, pageIndex) {
					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (getPageNumber() == pageable.getCurrentPage())
							tag.append("class", "active", " ");
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						super.onClick(target);
						onAjaxEvent(target);
					}
				}.setAutoEnable(false);
			}
		};
		add(pagingNavigation);
	}


	protected void onAjaxEvent(AjaxRequestTarget target)
	{
		// Update a parental container of the pageable, this assumes that the pageable is a component.
		Component container = ((Component)pageable);
		while (container instanceof AbstractRepeater || !container.getOutputMarkupId())
		{
			Component parent = container.getParent();
			if (parent == null) {
				break;
			}
			container = parent;
		}
		target.add(container);

		// in case the navigator is not contained by the container, we have
		// to add it to the response
		if (!((MarkupContainer) container).contains(this, true))
		{
			target.add(this);
		}
	}
}
