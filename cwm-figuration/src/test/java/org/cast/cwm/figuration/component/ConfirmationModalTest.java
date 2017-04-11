/*
 * Copyright 2011-2016 CAST, Inc.
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
package org.cast.cwm.figuration.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfirmationModalTest extends WicketTestCase {

	int clicks = 0;

	@Before
	public void setUpData() {
		clicks = 0;
	}

	@Test
	public void canRender() {
		tester.startComponentInPage(new TestConfirmationModal("id"));
		tester.assertComponent("id", ConfirmationModal.class);
	}

	@Test
	public void hasExpectedTexts() {
		tester.startComponentInPage(new TestConfirmationModal("id"));
		tester.assertContains("Confirmation</h4>");
		tester.assertContains("Confirm</button>");
		tester.assertContains("Cancel</button>");
		tester.assertContains("Do you really");
	}

	@Test
	public void callsOnConfirm() {
		tester.startComponentInPage(new TestConfirmationModal("id"));
		tester.clickLink("id:confirm");
		Assert.assertTrue("Didn't record link click", clicks==1);
	}

	private class TestConfirmationModal extends ConfirmationModal<Void> {

		public TestConfirmationModal(String id) {
			super(id);
		}

		@Override
		protected boolean onConfirm(AjaxRequestTarget target) {
			clicks++;
			return true;
		}
	}

}
