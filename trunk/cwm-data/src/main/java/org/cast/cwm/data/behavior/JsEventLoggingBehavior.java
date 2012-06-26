package org.cast.cwm.data.behavior;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.cast.cwm.service.EventService;

/**
 * A generic behavior that allows JS events to be logged
 */
public class JsEventLoggingBehavior extends AbstractDefaultAjaxBehavior {
	private static final long serialVersionUID = 1L;
	

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
		EventService.get().saveEvent(eventType, eventDetail, eventPage);
	}

}