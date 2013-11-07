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
package org.cast.cwm.data.component;

import static org.junit.Assert.assertEquals;

import java.util.List;

import lombok.Getter;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.Response;
import org.cast.cwm.test.CwmWicketTester;
import org.junit.Before;
import org.junit.Test;

public class AjaxDeletePersistedObjectDialogTest {

	private CwmWicketTester tester;

	@Before
	public void setUp() {
		tester = new CwmWicketTester();
	}
	
	@Test
	public void canRender() {
		tester.startComponentInPage(getResponseDeletingDialog());
		tester.assertComponent("dialog", AjaxDeletePersistedObjectDialog.class);
		tester.assertComponent("dialog:dialogBorder", DialogBorder.class);
		assertEquals("Delete should not have been called yet", 0,
				((TestDeletePersistedObjectDialog) tester.getComponentFromLastRenderedPage("dialog")).getDeleteCount());
	}

	@Test
	public void canDelete() {
		tester.startComponentInPage(getResponseDeletingDialog());
		//tester.debugComponentTrees();
		tester.assertComponent("dialog:dialogBorder:contentContainer", WebMarkupContainer.class);
		tester.clickLink("dialog:dialogBorder:contentContainer:dialogBorder_body:deleteLink");
		assertEquals("Delete should have been called", 1,
				((TestDeletePersistedObjectDialog) tester.getComponentFromLastRenderedPage("dialog")).getDeleteCount());
	}

	
	public void cancelLinkHasProperBehavior() {
		tester.startComponentInPage(getResponseDeletingDialog());
		tester.assertComponent("dialog:dialogBorder:contentContainer:dialogBorder_body:cancelLink", WebMarkupContainer.class);
		Component cancel = tester.getComponentFromLastRenderedPage("dialog:dialogBorder:contentContainer:dialogBorder_body:cancelLink");
		List<? extends Behavior> behaviors = cancel.getBehaviors();
		assertEquals("Should only have one behavior", 1, behaviors.size());
		// TODO - the Behavior should be replaced by an AttributeModifier for easier testing.
	}
	
	private AjaxDeletePersistedObjectDialog<Response> getResponseDeletingDialog() {
		return new TestDeletePersistedObjectDialog("dialog", getResponseModelToDelete());
	}
	
	private IModel<Response> getResponseModelToDelete() {
		return Model.of(new Response());
	}
	
	
	private class TestDeletePersistedObjectDialog extends AjaxDeletePersistedObjectDialog<Response> {

		private static final long serialVersionUID = 1L;

		public TestDeletePersistedObjectDialog(String id, IModel<Response> model) {
			super(id, model);
		}
		
		@Getter
		int deleteCount = 0;

		@Override
		protected void deleteObject(AjaxRequestTarget target) {
			deleteCount ++;
		}
		
	}
}
