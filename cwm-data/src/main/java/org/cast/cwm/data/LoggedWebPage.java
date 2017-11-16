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
package org.cast.cwm.data;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.data.component.IEventDataContributor;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;

import java.util.Date;

/**
 * A web page whose usage will be logged using the cwm-data event logging system.
 *
 * Every time the page is rendered, if a user is logged in, an Event will be created and stored.
 * Such pages must have a defined "name", which is stored as in the Event's "page" field.
 *
 * In addition, Javascript is added to the page to record the time taken to load the page, and
 * the time the user leaves the page.  If the extra AJAX call that this requires is not desired,
 * override {@link #shouldLogTiming()} to return false.
 */
@Slf4j
public abstract class LoggedWebPage<E extends Event> extends WebPage implements IEventDataContributor<E> {

	@Inject
	private IEventService eventService;

	@Inject
	private ICwmSessionService cwmSessionService;

	@Inject
	private ICwmService cwmService;

	@Getter
	private long pageViewEventId = -1;

	private final TimingInformationReceiver timingInformationReceiver;

	public LoggedWebPage (PageParameters parameters) {
		super(parameters);

		timingInformationReceiver = new TimingInformationReceiver();
		add(timingInformationReceiver);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		// Set up the javascript to log load time and end time of page view.
		if (shouldLogPageView() && shouldLogTiming()) {
			response.render(JavaScriptHeaderItem.forReference(
					new PackageResourceReference(LoggedWebPage.class, "CwmPageTiming.js")));
			response.render(OnLoadHeaderItem.forScript(
					String.format("CwmPageTiming.trackPage(%d, '%s')",
							pageViewEventId, timingInformationReceiver.getCallbackUrl())));
		}
		super.renderHead(response);
	}

	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		if (shouldLogPageView())
			pageViewEventId = storePageViewEvent();
	}

	/**
	 * Determine whether a page-view event should be logged for this render.
	 * By default, true if there is a logged-in session.
	 * @return true if an event should be logged.
	 */
	protected boolean shouldLogPageView() {
		return cwmSessionService.isSignedIn();
	}

	/**
	 * Determine whether the load time and end time of the page view should be logged as well as the start time.
	 * By default, always true but applications may wish to override it for certain page types.
	 * @return true if end time should be logged.
	 */
	protected boolean shouldLogTiming() {
		return true;
	}

	protected long storePageViewEvent() {
		IModel<? extends Event> mEvent = eventService.storePageViewEvent(getPageName(), this);
		return mEvent.getObject().getId();
	}

	@Override
	public void contributeEventData(E event) {
		event.setPage(getPageName());
		// TODO: consider if we want to add parentPageViewEvent tracking
//		if (pageViewEventId > 0)
//			event.setParentPageViewEvent(cwmService.getById(Event.class, pageViewEventId).getObject());
	}

	/**
	 * Defines the page identifier that will be stored into the "page" field of the page-view Event.
	 * @return a name for this page
	 */
	public abstract String getPageName();


	/**
	 * Receives information about page load times and end times via AJAX.
	 */
	protected class TimingInformationReceiver extends AbstractDefaultAjaxBehavior {

		@Override
		protected void respond(AjaxRequestTarget target) {
			IRequestParameters params = RequestCycle.get().getRequest().getRequestParameters();
			long id = params.getParameterValue("id").toLong();
			if (id != pageViewEventId)
				log.warn("Got id {}, but expecting {}", id, pageViewEventId);
			long loadTime = params.getParameterValue("loadTime").toLong();
			IModel<Event> mEvent = cwmService.getById(Event.class, id);
			if (mEvent != null && mEvent.getObject() != null) {
				mEvent.getObject().setLoadTime(loadTime);
				log.trace("Timing information received: loadTime={}; added to event {}", loadTime, id);
			}

			String endPageInfo = params.getParameterValue("endPageInfo").toOptionalString();
			if (!Strings.isEmpty(endPageInfo)) {
				for (String item : endPageInfo.split(";")) {
					String[] info = item.split("=");
					if (info.length == 3) {
						try {
							Long eventId = Long.valueOf(info[0]);
							Long duration = Long.valueOf(info[1]);
							Long inactiveDuration = Long.valueOf(info[2]);
							IModel<Event> mEndingEvent = cwmService.getById(Event.class, eventId);
							if (mEndingEvent != null && mEndingEvent.getObject() != null) {
								Event event = mEndingEvent.getObject();
								Date endTime = new Date(event.getStartTime().getTime() + duration);
								event.setEndTime(endTime);
								event.setInactiveDuration(inactiveDuration);
							} else {
								log.warn("End time information received but could not find event id={}", eventId);
							}
						} catch (NumberFormatException e) {
							log.warn("Ajax request contained invalid number, should be 'id=##=##': {}", item);
						}
					} else {
						log.warn("Ajax request contained invalid format, should be 'id=##=##': {}", item);
					}
				}

			}
			cwmService.flushChanges();
		}
	}

}
