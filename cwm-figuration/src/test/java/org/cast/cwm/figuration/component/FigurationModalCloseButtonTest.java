package org.cast.cwm.figuration.component;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author bgoldowsky
 */
public class FigurationModalCloseButtonTest extends WicketTestCase {

	@Test
	public void canRender() {
		tester.startComponentInPage(new FigurationModalCloseButton("id"));
		tester.assertComponent("id", FigurationModalCloseButton.class);
	}

	@Test
	public void snapshotTest() throws Exception {
		tester.startComponentInPage(new FigurationModalCloseButton("id"));
		tester.assertResultPage(getClass(),"snapshot/FigurationModalCloseButtonTest.html");
	}

}
