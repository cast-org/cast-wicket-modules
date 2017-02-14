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

	private WicketTester tester;

	@Before
	public void setUp() {
		tester = new WicketTester();
	}

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
