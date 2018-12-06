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
package pagination;

import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.util.tester.TagTester;
import org.cast.cwm.figuration.pagination.FigurationAjaxPagingNavigator;
import org.junit.Test;

import java.util.List;

/**
 * @author bgoldowsky
 */
public class FigurationAjaxPagingNavigatorTest extends BasePaginationTestCase {

	@Test
	public void canRender() {
		DataView view = new LongDataView("view");
		tester.startComponentInPage(new FigurationAjaxPagingNavigator("id", view));
		tester.assertComponent("id", FigurationAjaxPagingNavigator.class);
		List<TagTester> links = tester.getTagsByWicketId("pageLink");
		assertEquals("Should generate 10 nav links", 10, links.size());
		assertEquals("First link should be active", "page-link active",
				links.get(0).getAttribute("class"));
		assertEquals("Links should have class page-link", "page-link",
				links.get(1).getAttribute("class"));
		assertEquals("Link should go to page 3", "Go to page 3",
				links.get(2).getAttribute("title"));
		assertEquals("Link should be labeled 4", "4",
				links.get(3).getChild("span").getValue());
	}

	@Test
	public void showsMiddlePages() {
		DataView view = new LongDataView("view");
		view.setCurrentPage(20); // nav will show 17-26
		tester.startComponentInPage(new FigurationAjaxPagingNavigator("id", view));
		tester.assertComponent("id", FigurationAjaxPagingNavigator.class);
		tester.assertContainsNot("Go to Page 1");
		tester.assertContains("Go to page 17");
		tester.assertContains("Go to page 26");
	}


}
