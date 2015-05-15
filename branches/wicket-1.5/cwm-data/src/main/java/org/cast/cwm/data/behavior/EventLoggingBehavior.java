/*
 * Copyright 2011-2015 CAST, Inc.
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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.CwmSession;
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
	private static final String queryVar = "EventLoggingBehaviorDetailParameter";
	
	/**
	 * The event code.
	 */
	private String eventCode;
	
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
	
	/**
	 * Javascript to be executed on the client side before
	 * the AJAX logging request.
	 */
	@Getter @Setter
	private String javascript;
	
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

	/**
	 * Constructor
	 * 
	 * @param jsEvent the client-side event (e.g. onclick)
	 * @param eventCode the logging event code
	 */
	public EventLoggingBehavior(String jsEvent, String eventCode) {
		super(jsEvent);
		this.eventCode = eventCode;
		Injector.get().inject(this);
	}
	
	@Override
	protected CharSequence getCallbackScript() {
		if (queryStringExpression == null)
			return super.getCallbackScript();
		else
			return generateCallbackScript("wicketAjaxGet('" + 
					getCallbackUrl() + "&" + queryVar + "=' + " + queryVar);
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
		if (CwmSession.get().isSignedIn())
			eventService.saveEvent(eventCode, finalDetail.toString(), pageName);
		else
			log.info("Event triggered for non-logged in user: {}, {}, {}", new Object[] {eventCode, finalDetail, pageName});
	}
	
	/**
	 * This method is final because of the option to add a pre-pending script and
	 * client-side detail.  While this limits future flexibility, it makes the 
	 * common case MUCH easier.
	 */
	@Override
	final protected IAjaxCallDecorator getAjaxCallDecorator() {
		
		final StringBuffer preScript = new StringBuffer();
		
		if (javascript != null) {
			preScript.append(javascript).append("; ");
		}
		if (queryStringExpression != null)
			preScript.append("var " + queryVar + " = " + queryStringExpression + "; ");
		
		if (preScript.length() > 0) {
			return new AjaxCallDecorator() {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public CharSequence decorateScript(Component c, CharSequence script) {
					return preScript.toString() + script;
				}
			};
		} else {
			return super.getAjaxCallDecorator();
		}
	}
}
