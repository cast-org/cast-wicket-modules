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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.cast.cwm.IAppConfiguration;
import org.cast.cwm.data.*;
import org.cast.cwm.data.provider.AuditDataProvider;
import org.cast.cwm.data.provider.AuditTriple;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.ISiteService;
import org.cast.cwm.test.CwmDataInjectionTestHelper;
import org.cast.cwm.test.CwmDataTestCase;
import org.cast.cwm.test.CwmTestApplication;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.junit.Before;
import org.junit.Test;

public class TestUserContentLogPage extends CwmDataTestCase {

	@Override
	public void populateInjection(CwmDataInjectionTestHelper helper) {
		helper.injectAppConfiguration(this);
		helper.injectCwmSessionService(this);
		helper.injectFigurationService(this);

		ISiteService siteService = helper.injectMock(ISiteService.class);
		when(siteService.listSites()).thenReturn(getMockSiteList());
	}

	@Override
	public void setUpData() {
		super.setUpData();
		loggedInUser.setRole(Role.ADMIN);
	}

	@Test
	public void pageRendersSuccessfully() {
		//start and render the test page
		tester.startPage(MockUserContentLogPage.class, new PageParameters());

		//assert rendered page class
		tester.assertRenderedPage(UserContentLogPage.class);
		tester.assertContains("User Content Log");
		
		// Should have data from our mock object
		tester.assertContains("mock-subject-id");
		tester.assertContains("type");
		tester.assertContains("title");
		tester.assertContains("sample usercontent text");
		
		// Should have a table with one line of data
		tester.assertComponent("table", DataTable.class);
		tester.assertComponent("table:body:rows:1:cells:1:cell", Label.class);
		WebMarkupContainer w = ((WebMarkupContainer)tester.getComponentFromLastRenderedPage("table:body:rows"));
		assertEquals("Table should only have one row", 1, w.size());
		
	}
	
	public IModel<List<Site>> getMockSiteList() {
		return new AbstractReadOnlyModel<List<Site>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public List<Site> getObject() {
				return new ArrayList<Site>();
			}
		};
	}
	

	public static class MockUserContentLogPage extends UserContentLogPage {
		
		private static final long serialVersionUID = 1L;
		
		public MockUserContentLogPage(PageParameters parameters) {
			super(parameters);
		}

		@SuppressWarnings("unchecked")
		@Override
		public AuditDataProvider<UserContent, DefaultRevisionEntity> getDataProvider() {
			AuditDataProvider<UserContent,DefaultRevisionEntity> mockProvider = mock(AuditDataProvider.class);
			when(mockProvider.size()).thenReturn(1L);
			when(mockProvider.model(any(AuditTriple.class))).thenCallRealMethod(); // wraps argument in a model.
			doReturn(getMockDataIterator()).when(mockProvider).iterator(anyInt(), anyInt());
			return mockProvider;
		}

		private Iterator<? extends AuditTriple<UserContent, DefaultRevisionEntity>> getMockDataIterator() {
			User mockUser = mock(User.class);
			when(mockUser.getSubjectId()).thenReturn("mock-subject-id");
			
			UserContent userContent = mock(UserContent.class);
			when(userContent.getId()).thenReturn(1L);
			when(userContent.getUser()).thenReturn(mockUser);
			when(userContent.getDataType()).thenReturn(new IContentType() {
				@Override
				public String name() {
					return "TEXT";
				}
				@Override
				public String getDisplayName() {
					return "Text";
				}
			});
			when(userContent.getSortOrder()).thenReturn(1);
			when(userContent.getTitle()).thenReturn("title");
			when(userContent.getText()).thenReturn("sample usercontent text");
			
			DefaultRevisionEntity revisionEntity = mock(DefaultRevisionEntity.class);
			when(revisionEntity.getId()).thenReturn(1);
			when(revisionEntity.getRevisionDate()).thenReturn(new Date());

			AuditTriple<UserContent,DefaultRevisionEntity> triple = new AuditTriple<UserContent, DefaultRevisionEntity>(userContent, revisionEntity, RevisionType.ADD);
			
			ArrayList<AuditTriple<UserContent,DefaultRevisionEntity>> list = new ArrayList<AuditTriple<UserContent,DefaultRevisionEntity>>(1);
			list.add(triple);
			return list.iterator();
		}

	}
		
	
}
