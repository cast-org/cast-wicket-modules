package org.cast.cwm.figuration.component;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author bgoldowsky
 */
public class FigurationPopoverTest extends WicketTestCase {

	@Test
	public void canRender() {
		tester.startComponentInPage(new FigurationPopover<>("id")
				.setContent(new PopoverBodyPanel("content")));
		tester.assertComponent("id", FigurationPopover.class);
		tester.assertComponent("id:content", PopoverBodyPanel.class);
		tester.assertContains("Test popover body content.");
	}

	@Test
	public void snapshotTest() throws Exception {
		tester.startComponentInPage(new FigurationPopover<>("id")
				.setContent(new PopoverBodyPanel("content")));
		tester.assertResultPage(getClass(),"snapshot/FigurationModalTest.html");
	}

	private class PopoverBodyPanel extends Panel {

		public PopoverBodyPanel(String id) {
			super(id);
		}
	}

}
