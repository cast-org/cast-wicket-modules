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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of {@link AbstractEventService} that logs events as DEBUG messages
 * in the console.
 * 
 * @author jbrookover
 *
 */
public class LoggingEventService extends AbstractEventService {

	private final static Logger log = LoggerFactory.getLogger(LoggingEventService.class);
	
	public static LoggingEventService get() {
		return (LoggingEventService) instance;
	}
	
	public static void setInstance(LoggingEventService instance) {
		LoggingEventService.instance = instance;
	}
	
	@Override
	public Object saveEvent(String type, String detail, String location) {
		log.debug("Event: {}, {}, {}", Arrays.asList(type, detail, location));
		return null;
	}

}
