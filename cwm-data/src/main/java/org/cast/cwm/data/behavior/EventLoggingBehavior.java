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
package org.cast.cwm.data.behavior;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * An Ajax Behavior for logging events.  Supports executing
 * client-side script before the Ajax logging call as well
 * as evaluating a client-side expression that will be appended
 * to the detail of the event (e.g. gathering page state).
 * 
 * @author jbrookover
 *
 */
public class EventLoggingBehavior extends AjaxEventBehavior {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(EventLoggingBehavior.class);
	private static final String queryVar = "EventDetail";
	
	/**
	 * The event code.
	 */
	private String eventType;
	
	/**
	 * The detail of the event.
	 */
	@Getter @Setter
	private String detail;
	
	/**
	 * The name of the page, for the event log.
	 */
	@Getter @Setter
	private String pageName;
	
//	/**
//	 * Javascript to be executed on the client side before
//	 * the AJAX logging request.
//	 * 
//	 * TODO, bring this back, with a more descriptive name, if it's actually used.
//	 * Will require an AjaxCallListener, see https://cwiki.apache.org/confluence/display/WICKET/Wicket+Ajax
//	 */
//	@Getter @Setter
//	private String javascript;
	
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
	private String queryStringExpression;
	
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
	public EventLoggingBehavior(String jsEvent, String eventType) {
		super(jsEvent);
		this.eventType = eventType;
		Injector.get().inject(this);
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
	    super.updateAjaxAttributes(attributes);

	    if (queryStringExpression != null) {
		    // Add detail string dynamically
		    attributes.getDynamicExtraParameters().add(String.format("{ %s : %s }",
		    		queryVar, queryStringExpression));
	    }
	}
	
	@Override
	protected void onEvent(AjaxRequestTarget target) {
		// Construct Detail
		StringBuffer finalDetail = new StringBuffer();
		if (!Strings.isEmpty(detail))
			finalDetail.append(detail);

		// Additional detail from client side
		String clientSideInfo = RequestCycle.get().getRequest().getQueryParameters().getParameterValue(queryVar).toString();
		if (!Strings.isEmpty(clientSideInfo)) {
			if (finalDetail.length() > 0)
				finalDetail.append(":");
			finalDetail.append(clientSideInfo);
		}
		
		// Log Event
		if (cwmSessionService.isSignedIn())
			eventService.saveEvent(eventType, finalDetail.toString(), pageName);
		else
			log.info("Event triggered for non-logged in user: {}, {}, {}", new Object[] {eventType, finalDetail, pageName});
	}
	
}
