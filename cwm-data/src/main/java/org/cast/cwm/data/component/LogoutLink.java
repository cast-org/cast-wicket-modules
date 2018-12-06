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
package org.cast.cwm.data.component;

import com.google.inject.Inject;
import org.apache.wicket.Application;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.cast.cwm.CwmSession;
import org.cast.cwm.service.ICwmSessionService;

/**
 * Link that will terminate the current session and return the user to the login page.
 */
public class LogoutLink extends StatelessLink<Void> {
	
	@Inject
	private ICwmSessionService cwmSessionService;

	public LogoutLink(String id) {
		super(id);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible (cwmSessionService.isSignedIn());
	}

	@Override
	public void onClick() {
		CwmSession.get().signOut(this);
		setResponsePage(Application.get().getHomePage());
	}

}
