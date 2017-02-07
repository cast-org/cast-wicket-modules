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
package org.cast.cwm.service;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;
import org.cast.cwm.IEventLogger;
import org.cast.cwm.IEventType;
import org.cast.cwm.data.Event;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.data.component.IEventDataContributor;

import java.util.Date;
import java.util.List;

public interface IEventService extends IEventLogger {

	/** 
	 * Create a new Event instance (or a subclass of Event, according to the application's needs).
	 * @return the new object
	 */
	Event newEvent();

	/**
	 * Create a new instance of LoginSession (or a subclass, as appropriate for the application).
	 * @return the new object
	 */
	LoginSession newLoginSession();

	/**
	 * Return the enumeration used for event types by the application
	 * @return event type class object
	 */
	Class<? extends IEventType> getEventTypeClass();

	/**
	 * Look up the IEventType instance with the given name.
	 * @param typeName the name of an event type
	 * @return the type object, or null if it does not exist.
	 */
	IEventType getEventType(String typeName);

	/**
	 * Return a list of all defined event types.
	 * Used for creating filters in the Event Log page.
	 *
	 * @return list of type names
	 */
	List<? extends IEventType> listEventTypes();

	/**
	 * Save event to DB, after allowing all containing Components to embellish it.
	 * Before committing to database, each Component, starting with the Page
	 * and working down to the triggeringComponent, can add any relevant contextual
	 * information to the Event.  Any component that wants to add information should
	 * implement the {@link IEventDataContributor} interface.
	 *
	 * @param event the Event to be filled out and saved
	 * @param triggeringComponent the Component where the event was initiated (eg, a button)
	 * @return the persisted Event wrapped in a model
	 */
	<T extends Event> IModel<T> storeEvent(T event, Component triggeringComponent);

	/**
	 * Create and store an event.
	 * Basic information is passed in to this method, and additional information will be collected
	 * from the trigger component and its ancestors.
	 * @see #storeEvent(Event, Component)
	 *
	 * @param triggerComponent the Component where the event was initiated (eg, a button)
	 * @param type the type of the event
	 * @param detail additional information about the event
	 * @return the persisted Event wrapped in a model
	 */
	IModel<? extends Event> storeEvent(Component triggerComponent, IEventType type, String detail);

	/**
	 * Create and store an event to record a user logging in.
	 * @param triggerComponent login-causing component
	 * @return the persisted Event wrapped in a model
	 */
	IModel<? extends Event> storeLoginEvent(Component triggerComponent);

	/**
	 * Store an event representing opening a dialog.
	 * @param triggerComponent the button clicked to open the dialog
	 * @return the event logged
	 */
	IModel<? extends Event> storeDialogOpenEvent(Component triggerComponent);

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
	LoginSession createLoginSession(Request r);

	/**
	 * Lookup a LoginSession by the HTTP ID assigned to it by the web application container.
	 * 
	 * @param httpSessionId web server's ID for the session
	 * @return model of the LoginSession (model object may be null if no such LoginSession exists)
	 */
	IModel<LoginSession> getLoginSessionBySessionId(String httpSessionId);
	
	/**
	 * Mark the LoginSession as having ended at the given time.
	 * @param loginSession session object to close
	 * @param closeTime end-time to store
	 */
	void closeLoginSession(LoginSession loginSession, Date closeTime);

	/**
	 * Close the current {@link LoginSession} and save a Logout Event
	 * @return the stored event
	 */
	IModel<? extends Event> recordLogout(Component triggerComponent);

	/**
	 * 
	 * Closes a LoginSession without a logout - timeout or server shutdown. This will
	 * also save an event to mark the timeout.  
	 * 
	 * @param loginSession the LoginSession to be closed
	 * @param comment added to the event detail field
	 */
	void forceCloseLoginSession(LoginSession loginSession, String comment);

	/**
	 * Get the date of most recent event in a LoginSession.  
	 * Use sparingly; this requires a database query across all events in the LoginSession.
	 */
	Date getLastEventTime(LoginSession ls);

}