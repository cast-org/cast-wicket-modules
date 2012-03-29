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
package org.cast.cwm;

/**
 * Abstract base class for an event-logging functionality.
 * Many CWM modules will look for an instance of this class and, if found, 
 * call saveEvent to record some user activity.
 * 
 * Applications can extend this class and call the setInstance method to register
 * their object as the event logger that will be used.
 *
 */
public abstract class AbstractEventService {
	
	protected static AbstractEventService instance = new LoggingEventService();
	
	public static AbstractEventService get() {
		return instance;
	}
	
	public static void setInstance (AbstractEventService inst) {
		instance = inst;
	}
	
	/**
	 * Log an event with the appropriate details.
	 * 
	 * @param type
	 * @param detail
	 * @param location
	 * @return
	 */
    public abstract Object saveEvent (String type, String detail, String location);
    
}
