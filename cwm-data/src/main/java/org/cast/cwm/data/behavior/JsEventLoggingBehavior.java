/*
 * Copyright 2011 CAST, Inc.
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

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.cast.cwm.service.IEventService;

import com.google.inject.Inject;

/**
 * A generic behavior that allows JS events to be logged
 */
public class JsEventLoggingBehavior extends AbstractDefaultAjaxBehavior {
	private static final long serialVersionUID = 1L;

	@Inject
	private IEventService eventService;
	
	public JsEventLoggingBehavior() {
		InjectorHolder.getInjector().inject(this);
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		String eventDetail = RequestCycle.get().getRequest().getParameter("eventDetail");
		String eventType = RequestCycle.get().getRequest().getParameter("eventType");
		String eventPage = RequestCycle.get().getRequest().getParameter("eventPage");
		logJsEvent(eventDetail, eventType, eventPage);		
	}
	
	/**
	 * Override to customize logging
	 * 
	 * @param eventDetail
	 * @param eventType
	 * @param eventPage
	 */
	protected void logJsEvent(String eventDetail, String eventType, String eventPage) {
		if (eventPage == null || eventPage.equals(""))
			eventPage = getEventPage();
		eventService.saveEvent(eventType, eventDetail, eventPage);
	}

	/**
	 * Override this to setup default page information
	 * @return
	 */
	protected String getEventPage() {
		return null;
	}

	public void renderHead(IHeaderResponse response) {
		// setup a global js variable with the callback URL
		response.renderJavascript("var logJsEventCallbackUrl = '" + this.getCallbackUrl(false) + "';", "logJsEventCallbackUrl");
		super.renderHead(response);
	}		
}