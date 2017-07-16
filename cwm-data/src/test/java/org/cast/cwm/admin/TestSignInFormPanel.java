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
package org.cast.cwm.admin;

import org.apache.wicket.Session;
import org.apache.wicket.devutils.stateless.StatelessChecker;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.cast.cwm.CwmSession;
import org.cast.cwm.IAppConfiguration;
import org.cast.cwm.data.*;
import org.cast.cwm.data.provider.AuditDataProvider;
import org.cast.cwm.data.provider.AuditTriple;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.ISiteService;
import org.cast.cwm.test.CwmDataInjectionTestHelper;
import org.cast.cwm.test.CwmDataTestApplication;
import org.cast.cwm.test.CwmDataTestCase;
import org.cast.cwm.test.CwmTestApplication;
import org.cwm.db.service.IModelProvider;
import org.cwm.db.service.SimpleModelProvider;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class TestSignInFormPanel extends CwmDataTestCase {

	@Override
	public void populateInjection(CwmDataInjectionTestHelper helper) {
		helper.injectMock(IEventService.class);
		helper.injectObject(IModelProvider.class, new SimpleModelProvider());
	}

	@Test
	public void canRender() {
		tester.startComponentInPage(new SignInFormPanel("id"));
		tester.assertComponent("id", SignInFormPanel.class);
	}

	@Test
	public void isStateless() {
		tester.getApplication().getComponentPostOnBeforeRenderListeners()
				.add(new StatelessChecker());
		tester.startComponentInPage(new SignInFormPanel("id"));
		tester.assertComponent("id", SignInFormPanel.class);
		assertTrue("Not stateless", tester.getComponentFromLastRenderedPage("id").isStateless());
	}

	@Test
	public void hasRequiredFields() {
		tester.startComponentInPage(new SignInFormPanel("id"));
		FormTester formtest = tester.newFormTester("id:form", true);
		formtest.submit();
		tester.assertErrorMessages("'username' is required.", "'password' is required.");
	}

	@Test
	public void rejectsIncorrectPassword() {
		tester.startComponentInPage(new SignInFormPanel("id"));
		FormTester formtest = tester.newFormTester("id:form", false);
		formtest.setValue("usernameBorder:usernameBorder_body:username", "user");
		formtest.setValue("passwordBorder:passwordBorder_body:password", "pass");
		formtest.submit();
		tester.assertErrorMessages("Invalid username and/or password.");
	}

	@Test
	public void acceptsCorrectPassword() {
		tester.startComponentInPage(new SignInFormPanel("id"));
		FormTester formtest = tester.newFormTester("id:form", false);
		formtest.setValue("usernameBorder:usernameBorder_body:username", "user");
		formtest.setValue("passwordBorder:passwordBorder_body:password", "correct-password");
		formtest.submit();
		tester.assertNoErrorMessage();
	}


	protected MockApplication newApplication() {
		CwmDataTestApplication app = new MyApplication(injectionHelper.getMap());
		app.setApplicationUsesThemeDir(isApplicationThemed());
		return app;
	}

	/**
	 * We need an application that uses actual CwmSession objects,
	 * and will return a mock User.
 	 */
	private class MyApplication extends CwmDataTestApplication {

		public MyApplication(Map<Class<?>, Object> map) {
			super(map);
		}

		@Override
		public Session newSession(Request request, Response response) {
			return new CwmSession(request);
		}

		@Override
		public User getUser(String username) {
			User user = new User();
			user.setValid(true);
			user.setPassword("correct-password");
			return user;
		}
	}

}
