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
package org.cast.cwm.data.behavior;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.IEventType;
import org.cast.cwm.data.Event;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;

/**
 * An Ajax Behavior for logging events.  Supports executing
 * client-side script before the Ajax logging call as well
 * as evaluating a client-side expression that will be appended
 * to the detail of the event (e.g. gathering page state).
 * 
 * @author jbrookover
 * @author bgoldowsky
 */
@Slf4j
public class EventLoggingBehavior extends AjaxEventBehavior {

	private static final String queryVar = "EventDetail";
	
	/**
	 * The event code.
	 */
	private IEventType eventType;
	
	/**
	 * The base detail string for the event.
	 */
	@Getter @Setter
	private String detail;
	
	/**
	 * Expression that will be evaluated on the client and
	 * appended to the 'detail' field of the event.  For example, 
	 * if this behavior is attached to a toggle button, determine
	 * whether the toggle is on or off.  Code could look like this:
	 * 
	 * <pre>
	 *    $(this).parent().is(':visible') ? 'close' : 'open'
	 * </pre>
	 */
	@Getter @Setter
	private String detailsExpression;
	
	@Inject
	private IEventService eventService;
	
	@Inject
	private ICwmSessionService cwmSessionService;

	/**
	 * Constructor
	 * 
	 * @param jsEvent the client-side event (e.g. onclick)
	 * @param eventType the logging event code
	 */
	public EventLoggingBehavior(String jsEvent, IEventType eventType) {
		super(jsEvent);
		this.eventType = eventType;
		Injector.get().inject(this);
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
	    super.updateAjaxAttributes(attributes);

	    if (detailsExpression != null) {
		    // Add detail string dynamically
		    attributes.getDynamicExtraParameters().add(String.format("return { %s : %s }",
		    		queryVar, detailsExpression));
	    }
	}
	
	@Override
	protected void onEvent(AjaxRequestTarget target) {
		if (cwmSessionService.isSignedIn()) {
			Event event = eventService.newEvent();
			event.setType(eventType);
			event.setDetail(constructEventDetail());
			onBeforeSaveEvent(event);
			eventService.storeEvent(event, this.getComponent());
			onAfterSaveEvent(event);
		} else {
			log.info("Event triggered for non-logged in user: {}, {}", eventType, constructEventDetail());
		}
	}

	/**
	 * Build a details string to be saved for this event.
	 * This will contain the "detail" string given in the constructor, if any, plus whatever is returned by the
	 * client-side "detailsExpression" javascript.
	 * @return string that will be logged as the event details field
	 */
	protected String constructEventDetail() {
		StringBuilder finalDetail = new StringBuilder();
		if (!Strings.isEmpty(detail))
			finalDetail.append(detail);

		// Additional detail from client side
		String clientSideInfo = getCustomParameterValue(queryVar);
		if (!Strings.isEmpty(clientSideInfo)) {
			if (finalDetail.length() > 0)
				finalDetail.append(":");
			finalDetail.append(clientSideInfo);
		}
		return finalDetail.toString();
	}

	protected String getCustomParameterValue(String parameterName) {
		return RequestCycle.get().getRequest().getQueryParameters().getParameterValue(parameterName).toString();
	}

	/**
	 * Provided so that subclasses can add to or modify the event to be stored.
	 * @param event event object that can be modified before being saved
	 */
	protected void onBeforeSaveEvent(Event event) {
	}

	/**
	 * Provided so that subclasses can add post Save.
	 * @param event event object that can be modified before being saved
	 */
	protected void onAfterSaveEvent(Event event) {
	}
}
