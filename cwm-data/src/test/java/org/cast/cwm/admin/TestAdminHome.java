/*
 * Copyright 2011-2018 CAST, Inc.
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

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Role;
import org.cast.cwm.service.AdminPageService;
import org.cast.cwm.service.IAdminPageService;
import org.cast.cwm.test.CwmDataInjectionTestHelper;
import org.cast.cwm.test.CwmDataTestCase;
import org.junit.Test;

public class TestAdminHome extends CwmDataTestCase {

	@Override
	public void populateInjection(CwmDataInjectionTestHelper helper) {
		super.populateInjection(helper);
		helper.injectCwmSessionService(this);
		helper.injectAppConfiguration(this);
		helper.injectFigurationService(this);

		// We use the real service class here, it is safe for testing
		IAdminPageService adminPageService = helper.injectObject(IAdminPageService.class, new AdminPageService());
	}

	@Override
	public void setUpData() {
		super.setUpData();
		loggedInUser.setRole(Role.ADMIN);
	}
	
	@Test
	public void canRender() {
		tester.startPage(AdminHome.class);
		tester.assertRenderedPage(AdminHome.class);
		tester.assertContains("Administrator area");
		tester.assertLabel("category:1:header", "Accounts");
		tester.assertBookmarkablePageLink("category:1:linkRepeater:1:link", UserListPage.class, new PageParameters());
	}
	
}
