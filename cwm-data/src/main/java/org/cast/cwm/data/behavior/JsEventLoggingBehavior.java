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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;
import org.cast.cwm.service.IEventService;

import com.google.inject.Inject;

/**
 * A generic behavior that allows JS events to be logged
 */
public class JsEventLoggingBehavior extends AbstractDefaultAjaxBehavior {

	@Inject
	private IEventService eventService;
	
	public JsEventLoggingBehavior() {
		Injector.get().inject(this);
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		String eventDetail = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("eventDetail").toString();
		String eventType = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("eventType").toString();
		logJsEvent(eventDetail, eventType);
	}
	
	/**
	 * Override to customize logging
	 * 
	 * @param eventDetail
	 * @param eventType
	 */
	protected void logJsEvent(String eventDetail, String eventType) {
		eventService.storeEvent(this.getComponent(), eventType, eventDetail);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		// setup a global js variable with the callback URL
		response.render(JavaScriptHeaderItem.forScript("var logJsEventCallbackUrl = '" + this.getCallbackUrl() + "';", "logJsEventCallbackUrl"));
		super.renderHead(component, response);
	}		
}