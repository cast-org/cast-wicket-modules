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
package org.cast.cwm.data.event;

import lombok.Getter;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * Superclass for all Messages that are tied to Ajax events,
 * and thus have an AjaxRequestTarget.  A Message in CWM
 * is the payload of an {@link org.apache.wicket.event.IEvent}
 *
 */
public class AjaxMessage {

	@Getter
	private AjaxRequestTarget target;

	public AjaxMessage(AjaxRequestTarget target) {
		this.target = target;
	}

}
