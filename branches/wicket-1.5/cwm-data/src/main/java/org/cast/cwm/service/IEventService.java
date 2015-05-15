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
package org.cast.cwm.service;

import java.util.Date;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;
import org.cast.cwm.IEventLogger;
import org.cast.cwm.data.Event;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.data.ResponseData;

public interface IEventService extends IEventLogger {

	/** 
	 * Create a new Event instance (or a subclass of Event, according to the application's needs).
	 */
	public abstract Event newEvent();
	
	/**
	 * Save an event to the datastore.
	 * 
	 * @see {@link AbstractEventService#saveEvent(String, String, String)}
	 * @return model wrapping the event that was saved
	 */
	public abstract IModel<? extends Event> saveEvent(String type,
			String detail, String pageName);

	/**
	 * Save a login event.
	 * 
	 */
	public abstract IModel<? extends Event> saveLoginEvent();

	/**
	 * Save a page view event.
	 * 
	 * @param detail
	 * @param pageName
	 */
	public abstract IModel<? extends Event> savePageViewEvent(String detail, String pageName);

	/**
	 * Save a post event.  If this event has an accompanying {@link ResponseData} object,
	 * set hasResponses=true so the event log knows to look it up.
	 * 
	 * @param hasResponses 
	 * @return
	 */
	public abstract IModel<? extends Event> savePostEvent(boolean hasResponses,
			String pageName);


	/**
	 * Create a new instance of LoginSession (or a subclass, as appropriate for the application).
	 * @return
	 */
	public abstract LoginSession newLoginSession();

	/** 
	 * Create a LoginSession object in the database based on the current Session and the given Request.
	 * This should generally be called when a user logs in.
	 * Many details about the client and session will be filled in automatically if you put this in your 
	 * Application's init() method:
	 *   getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
	 * (see the javadoc for ClientProperties).	
	 * @param r the current Request object
	 * @return the created LoginSession
	 */
	public abstract LoginSession createLoginSession(Request r);

	/**
	 * Lookup a LoginSession by the HTTP ID assigned to it by the web application container.
	 * 
	 * @param httpSessionId
	 * @return model of the LoginSession (model object may be null if no such LoginSession exists)
	 */
	public IModel<LoginSession> getLoginSessionBySessionId(String httpSessionId);
	
	/**
	 * Mark the LoginSession as having ended at the given time.
	 * @param loginSession
	 * @param closeTime
	 */
	public abstract void closeLoginSession(LoginSession loginSession,
			Date closeTime);

	/**
	 * Close the current {@link LoginSession} and save a Logout Event
	 */
	public abstract void recordLogout();

	/**
	 * 
	 * Closes a LoginSession without a logout - timeout or server shutdown. This will
	 * also save an event to mark the timeout.  
	 * 
	 * @param loginSession the LoginSession to be closed
	 * @param comment added to the event detail field
	 */
	public abstract void forceCloseLoginSession(LoginSession loginSession, String comment);

	/**
	 * Return a list of all types of events currently found in the database.
	 * Used for creating filters in Event Log page.
	 * @return
	 */
	public abstract IModel<List<String>> getEventTypes();

	/**
	 * Get the date of most recent event in a LoginSession.  
	 * Use sparingly; this requires a database query across all events in the LoginSession.
	 */
	public abstract Date getLastEventTime(LoginSession ls);

}