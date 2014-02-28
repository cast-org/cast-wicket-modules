/*
 * Copyright 2011-2014 CAST, Inc.
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


/**
 * Interface for an object that can be used to log events.
 * Exists so that CWM modules can record some user activity without depending on
 * any more specific functionality; but currently unused.
 *
 */
public interface IEventLogger {

	/**
	 * Log an event with the appropriate details.
	 *
	 * @param type The type of event; should be a manageable finite set of types for each application
	 * @param detail Any additional details about the event
	 * @param location An indication of the page or area of the application where the event occurred
	 * @return if an object was created to represent the event, return it.
	 */
    public abstract Object saveEvent (String type, String detail, String location);
    
	/**
	 * Log a component-related event with the appropriate details.
	 *
	 * @param type The type of event; should be a manageable finite set of types for each application
	 * @param detail Any additional details about the event
	 * @param location An indication of the page or area of the application where the event occurred
	 * @param componentPath the wicket ID path to the component related to this event.
	 * @return if an object was created to represent the event, return it.
	 */
	public abstract Object saveEvent(String type, String detail, String pageName, String componentPath);


}
