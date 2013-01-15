package org.cast.cwm.components;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class ShyLabelTest {
	
	private WicketTester wicketTester;

	@Before
	public void setUp() {
		wicketTester = new WicketTester();
	}
	
	@Test
	public void canRenderWithAllContentShown() {
		wicketTester.startComponentInPage(new ShyLabel("id", new Model<String>("test")));
		wicketTester.assertComponent("id", ShyLabel.class);
		wicketTester.assertVisible("id");
	}
	
	@Test
	public void canRenderWithAllContentHidden() {
		wicketTester.startComponentInPage(new ShyLabel("id", new Model<String>("")));
		wicketTester.assertInvisible("id");
	}

	@Test
	public void canBeMadeInvisible() {
		ShyLabel label = new ShyLabel("id", new Model<String>("test"));
		wicketTester.startComponentInPage(label);
		wicketTester.assertComponent("id", ShyLabel.class);
		wicketTester.assertVisible("id");
		
		label.setDefaultModelObject("");
		label.render();
		wicketTester.assertInvisible("id");		
	}
	
	@Test
	public void canBeMadeVisible() {
		ShyLabel label = new ShyLabel("id", new Model<String>(""));
		wicketTester.startComponentInPage(label);
		wicketTester.assertInvisible("id");		

		label.setDefaultModelObject("test");
		label.render();
		
		wicketTester.assertComponent("id", ShyLabel.class);
		wicketTester.assertVisible("id");
	}
	
}
