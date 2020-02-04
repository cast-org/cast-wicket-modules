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
package org.cast.cwm.admin;

import com.google.inject.Inject;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.IEventType;
import org.cast.cwm.service.IEventService;

/**
 * Displays documentation of the event types.
 *
 */
@AuthorizeInstantiation("RESEARCHER")
public class EventLogDocumentationPage extends AdminPage {

	@Inject
	private IEventService eventService;

	public EventLogDocumentationPage(PageParameters parameters) {
		super(parameters);
		setPageTitle("Event Log Documentation");

		RepeatingView typeView = new RepeatingView("type");
		add(typeView);

		for (IEventType type : eventService.listEventTypes()) {
			WebMarkupContainer item = new WebMarkupContainer(typeView.newChildId());
			typeView.add(item);
			item.add(new Label("name", type.name()));
			item.add(new Label("displayName", type.getDisplayName()));
			item.add(new Label("documentation", type.getDocumentation()));
		}
	}

}
