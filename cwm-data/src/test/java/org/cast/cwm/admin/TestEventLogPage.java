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
package org.cast.cwm.admin;

import net.databinder.models.hib.ICriteriaBuilder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Event;
import org.cast.cwm.data.Site;
import org.cast.cwm.service.ISiteService;
import org.cast.cwm.test.CwmDataInjectionTestHelper;
import org.cast.cwm.test.CwmDataTestCase;
import org.cwm.db.service.IModelProvider;
import org.cwm.db.service.SimpleModelProvider;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestEventLogPage extends CwmDataTestCase {

	private ISiteService siteService;

	@Override
	public void populateInjection(CwmDataInjectionTestHelper helper) {
		super.populateInjection(helper);
		helper.injectCwmService(this);
		helper.injectCwmSessionService(this);
		helper.injectAppConfiguration(this);
		helper.injectFigurationService(this);
		helper.injectObject(IModelProvider.class, new SimpleModelProvider());
		siteService = helper.injectMock(ISiteService.class);
		when(siteService.listSites()).thenReturn(new ListModel<Site>(Collections.singletonList(mockSite())));
	}

	@Test
	public void canRender() {
		tester.startPage(TestableEventLogPage.class);

		//assert rendered page class
		tester.assertRenderedPage(EventLogPage.class);
		tester.assertContains("Event Log");
		
	}

	private Site mockSite() {
		Site site = new Site();
		site.setName("Test Site");
		site.setSiteId("site1");
		return site;
	}


	public static class TestableEventLogPage extends EventLogPage {

		public TestableEventLogPage(PageParameters params) {
			super(params);
		}

		@Override
		protected ISortableDataProvider<Event, String> makeDataProvider(ICriteriaBuilder builder) {
			ISortableDataProvider mock = mock(ISortableDataProvider.class);
			when(mock.getSortState()).thenReturn(new SingleSortState());
			return mock;
		}
	}

}
