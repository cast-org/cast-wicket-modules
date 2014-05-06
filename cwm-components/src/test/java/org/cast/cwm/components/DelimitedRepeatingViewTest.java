package org.cast.cwm.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class DelimitedRepeatingViewTest {

	private WicketTester tester;

	@Before
	public void setUp() {
		tester = new WicketTester();
	}
	
	@Test
	public void canRender() {
		tester.startComponentInPage(makeView());
		tester.assertComponent("view", DelimitedRepeatingView.class);
	}
	
	@Test
	public void showsAllItems() {
		tester.startComponentInPage(makeView());
		tester.assertContains("first item");
		tester.assertContains("second item");
		tester.assertContains("third item");
	}
	
	@Test
	public void includesDefaultDelimiter() {
		tester.startComponentInPage(makeView());
		tester.assertContains("first item</span> <span");		
	}
	
	@Test
	public void includesSettableDelimiter() {
		DelimitedRepeatingView view = makeView();
		view.setDelimiter("DeLimit");
		tester.startComponentInPage(view);
		tester.assertContains("first item</span>DeLimit<span");
	}

	private DelimitedRepeatingView makeView() {
		DelimitedRepeatingView view = new DelimitedRepeatingView("view");
		view.add(new Label(view.newChildId(), "first item"));
		view.add(new Label(view.newChildId(), "second item"));
		view.add(new Label(view.newChildId(), "third item"));
		return view;
	}
	
}
