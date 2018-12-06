/*
 * Copyright 2011-2019 CAST, Inc.
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

import java.io.Serializable;

/**
 * Applications must define an event-type enumerator implementing this interface.
 * Every stored event will have a type.
 * Event types have an internal name() for use in the database (eg the enum constant),
 * a display name (what researchers will see in the event log),
 * and a documentation string describing what it means.
 *
 * CWM itself requires that a few basic event types exist so that it can store events;
 * for example "login" and "logout" events that are reported by code in the cwm-data module.
 * There are abstract methods in EventService that must be implemented return these event types.
 */
public interface IEventType extends Serializable {

	String name();

	String getDisplayName();

	String getDocumentation();

}
