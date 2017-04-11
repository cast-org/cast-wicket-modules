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
package org.cast.cwm.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * @author bgoldowsky
 */
@Slf4j
public class ApproximateDateLabelTest extends WicketTestCase {

	@Test
	public void canRender() {
		tester.startComponentInPage(new ApproximateDateLabel("id", new Date()));
		tester.assertComponent("id", ApproximateDateLabel.class);
	}

	@Test
	public void snapshotTest() throws Exception {
		tester.startComponentInPage(new ApproximateDateLabel("id", new Date(1000)));
		tester.assertResultPage(getClass(),"snapshot/ApproximateDateLabel.html");
	}

	@Test
	public void showsToday() {
		tester.startComponentInPage(new ApproximateDateLabel("id", new Date(now())));
		tester.assertContains("\\d+:\\d+[AP]M Today");
	}

	@Test
	public void showsYesterday() throws Exception {
		Date date = new Date(now()-24*60*60*1000);
		tester.startComponentInPage(new ApproximateDateLabel("id", date));
		tester.assertContains("\\d+:\\d+[AP]M Yesterday");
	}

	private long now() {
		return new Date().getTime();
	}

}
