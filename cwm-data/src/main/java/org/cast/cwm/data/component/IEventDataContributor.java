/*
 * Copyright 2011-2017 CAST, Inc.
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
package org.cast.cwm.data.component;

import org.apache.wicket.Component;
import org.cast.cwm.data.Event;

/**
 * An object (usually a {@link org.apache.wicket.Component} that can add information about Events within its scope.
 * The contributeEventData(Event) method is called by {@link org.cast.cwm.service.EventService#storeEvent(Event, Component)}
 * for each ancestor of the event-triggering Component before the Event is saved.
 *
 * Parameter T is the subtype of Event that is expected.
 */
public interface IEventDataContributor<T extends Event> {

    /**
     * Add relevant information to the given Event.
     * @param event the Event to be modified.
     */
    void contributeEventData(T event);

}
