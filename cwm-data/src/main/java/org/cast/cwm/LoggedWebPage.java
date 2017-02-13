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
package org.cast.cwm;

import com.google.inject.Inject;
import lombok.Getter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Event;
import org.cast.cwm.data.component.IEventDataContributor;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;

/**
 * A web page whose usage will be logged using the cwm-data event logging system.
 *
 * Every time the page is rendered, if a user is logged in, an Event will be created and stored.
 * Such pages must have a defined "name", which is stored as in the Event's "page" field.
 */
public abstract class LoggedWebPage extends WebPage implements IEventDataContributor<Event> {

	@Inject
	private IEventService eventService;

	@Inject
	private ICwmSessionService cwmSessionService;

	@Getter
	private long pageViewEventId = -1;

	public LoggedWebPage (PageParameters parameters) {
		super(parameters);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		// TODO: set up the javascript to log load time and end time of page view.
		// It should probably be optional, some apps won't want to incur the extra server traffic.
		super.renderHead(response);
	}

	@Override
	protected void onAfterRender() {
		super.onAfterRender();
		if (cwmSessionService.isSignedIn())
			pageViewEventId = storePageViewEvent();
	}

	protected long storePageViewEvent() {
		IModel<? extends Event> mEvent = eventService.storePageViewEvent(getPageName());
		return mEvent.getObject().getId();
	}

	@Override
	public void contributeEventData(Event event) {
		event.setPage(getPageName());
		// TODO: consider if we want to add parentPageViewEvent tracking
//		if (pageViewEventId > 0)
//			event.setParentPageViewEvent(cwmService.getById(Event.class, pageViewEventId).getObject());
	}

	public abstract String getPageName();

}
