/*
 * Copyright 2011-2013 CAST, Inc.
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
package org.cast.cwm.data.behavior;

import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.cycle.RequestCycle;

public class ChromeFrameUtils {

	/**
	 * Call this method from a page constructor to activate Chrome Frame in IE.  In a non-IE
	 * browser, this will do nothing.
	 * 
	 * This will only use Chrome Frame for that page; IE will regain control if you leave the page.
	 */
	public static void useChromeFrame() {
		// FIXME - best way to set headers?
		// maybe this: RequestCycle.get().getResponse().getContainerResponse();
		
		((WebRequestCycle)WebRequestCycle.get()).getWebResponse().setHeader("X-UA-Compatible", "chrome=1");
	}

	/**
	 * <p>
	 * Is the user currently using Internet Explorer without Chrome Frame?  This can be spoofed, 
	 * so only use for the purposes of showing/hiding elements to install Chrome Frame.
	 * </p>
	 * 
	 * <p>
	 * <strong>Note:</strong> This will return False if the user has installed Chrome Frame.  <em>However</em>,
	 * this does not mean that Chrome Frame is activated.  You must call {@link #useChromeFrame()} in a 
	 * page constructor to activate Chrome.
	 * </p>
	 * 
	 * @return true if the user is using Internet Explorer without Chrome Frame
	 */
	public static boolean isBareIE() {
		return ((WebClientInfo)Session.get().getClientInfo()).getProperties().isBrowserInternetExplorer();
	}

}
