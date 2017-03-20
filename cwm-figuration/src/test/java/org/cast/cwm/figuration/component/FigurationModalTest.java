package org.cast.cwm.figuration.component;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * @author bgoldowsky
 */
public class FigurationModalTest extends WicketTestCase {

	@Test
	public void canRender() {
		tester.startComponentInPage(new FigurationModal<Void>("id")
				.withTitle("Test title")
				.withBody(new ModalBodyPanel("body"))
				.withEmptyFooter());
		tester.assertComponent("id", FigurationModal.class);
		tester.assertComponent("id:header", FigurationModalBasicHeader.class);
		tester.assertComponent("id:body", ModalBodyPanel.class);
		tester.assertComponent("id:footer", EmptyPanel.class);
		tester.assertContains("Test title");
		tester.assertContains("Test modal body content.");
	}

	@Test
	public void snapshotTest() throws Exception {
		tester.startComponentInPage(new FigurationModal<Void>("id")
				.withTitle("Test title")
				.withBody(new ModalBodyPanel("body"))
				.withEmptyFooter());
		tester.assertResultPage(getClass(),"snapshot/FigurationModalTest.html");
	}

	private class ModalBodyPanel extends Panel {

		public ModalBodyPanel(String id) {
			super(id);
		}
	}

}
