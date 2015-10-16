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
package org.cast.cwm.admin;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.IAppConfiguration;
import org.cast.cwm.data.component.LogoutLink;
import org.cast.cwm.service.ICwmSessionService;

import com.google.inject.Inject;

/**
 * Header Panel for Admin Pages.
 * 
 * @author jbrookover
 *
 */
public class AdminHeaderPanel extends Panel {
	
	@Inject
	ICwmSessionService cwmSessionService;

	private static final long serialVersionUID = 1L;

	public AdminHeaderPanel(String id) {
		super(id);
	
		String appNameHeader = "";
		if (Application.get() instanceof CwmApplication) {
			appNameHeader = CwmApplication.get().getAppAndInstanceId();
		}

		add(new Label("appName", appNameHeader));
		add(new BookmarkablePageLink<WebPage>("homeLink", Application.get().getHomePage()));
		add(new Label("name", cwmSessionService.getUser().getFullName()));
		add(new LogoutLink("logoutLink"));
	}
}
