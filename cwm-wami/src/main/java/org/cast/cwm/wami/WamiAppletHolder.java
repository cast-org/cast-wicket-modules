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
package org.cast.cwm.wami;

import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * This is a placeholder for the embedded SWF that is be used by {@link PlayerPanel} and {@link RecorderPanel}.
 * Put it on the page somewhere that it will not be removed or replaced by AJAX updates.
 * It should be a nice visible location, though, so that the settings panel can be seen. 
 *
 * @author bgoldowsky
 *
 */
public class WamiAppletHolder extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	public WamiAppletHolder(String id) {
		super(id);
		setOutputMarkupId(true);
	}

}
