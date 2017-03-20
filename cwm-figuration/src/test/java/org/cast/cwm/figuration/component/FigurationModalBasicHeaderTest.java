package org.cast.cwm.figuration.component;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author bgoldowsky
 */
public class FigurationModalBasicHeaderTest extends WicketTestCase {

	@Test
	public void canRender() {
		tester.startComponentInPage(new FigurationModalBasicHeader("id", Model.of("Test Heading")));
		tester.assertComponent("id", FigurationModalBasicHeader.class);
	}

	@Test
	public void showsHeader() {
		tester.startComponentInPage(new FigurationModalBasicHeader("id", Model.of("Test Heading")));
		tester.assertLabel("id:title", "Test Heading");
	}

	@Test
	public void showsCloseButton() {
		tester.startComponentInPage(new FigurationModalBasicHeader("id", Model.of("Test Heading")));
		tester.assertComponent("id:close", FigurationModalCloseButton.class);
	}

	@Test
	public void snapshotTest() throws Exception {
		tester.startComponentInPage(new FigurationModalBasicHeader("id", Model.of("Test Heading")));
		tester.assertResultPage(getClass(),"snapshot/FigurationModalBasicHeaderTest.html");
	}

}
