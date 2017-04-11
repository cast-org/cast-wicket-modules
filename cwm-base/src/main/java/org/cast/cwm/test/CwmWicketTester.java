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
package org.cast.cwm.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.LabeledWebMarkupContainer;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;

public class CwmWicketTester extends WicketTester {

	private final static Method getReplaceModelMethod;

	static {
		try {
			getReplaceModelMethod = AttributeModifier.class
					.getDeclaredMethod("getReplaceModel");
			getReplaceModelMethod.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public CwmWicketTester() {
		super();
	}

	public CwmWicketTester(Class<? extends Page> homePage) {
		super(homePage);
	}

	public CwmWicketTester(WebApplication application) {
		super(application);
	}

	public CwmWicketTester(WebApplication application, String path) {
		super(application, path);
	}

// TODO - how to test non-button ajax operations in wicket 1.5+ ?
// An answer may be here: http://stackoverflow.com/questions/6176615/how-to-test-ajaxformchoicecomponentupdatingbehavior-in-wickettester
//	public void executeAjaxBehavior(String path, String value) {
//		AbstractAjaxBehavior behavior = (AbstractAjaxBehavior) getComponentFromLastRenderedPage(
//				path).getBehaviors().get(0);
//		CharSequence url = behavior.getCallbackUrl();
//		RequestCycle cycle = setupRequestAndResponse(true);
//		getServletRequest().setRequestToRedirectString(url.toString());
//		String[] ids = path.split(":");
//		String id = ids[ids.length - 1];
//		getServletRequest().setParameter(id, value);
//		processRequestCycle(cycle);
//	}

	public void assertAttribute(String message, String expected,
			Component component, String attribute) {
		String value = getTagByWicketId(component.getId()).getAttribute(attribute);
		assertEquals(String.format("%s: expected \"%s\", got \"%s\"", message, expected, value), expected, value);
	}

	public void assertNotAttribute(String message, String expected,
			Component component, String attribute) {
		assertFalse(message, getTagByWicketId(component.getId()).getAttributeIs(attribute, expected));
	}

	public void assertErrorMessagesContain(String expectedMessage) {
		assertMessagesContain(expectedMessage, FeedbackMessage.ERROR);
	}

	public void assertInfoMessagesContain(String expectedMessage) {
		assertMessagesContain(expectedMessage, FeedbackMessage.INFO);
	}

	public void assertWarningMessagesContain(String expectedMessage) {
		assertMessagesContain(expectedMessage, FeedbackMessage.WARNING);
	}

	public void assertMessagesContain(String expectedMessage, int messageLevel) {
		List<Serializable> actualMessages = this
				.getMessages(messageLevel);
		List<Serializable> msgs = new ArrayList<Serializable>();
		for (Iterator<Serializable> iterator = actualMessages.iterator(); iterator
				.hasNext();) {
			msgs.add(iterator.next().toString());
		}
		assertTrue("Messages do not contain expected string '"
				+ expectedMessage + "'", msgs.contains(expectedMessage));
	}

	public void assertLabeledWebMarkupContainerLabel(String path, String labelText) {
		assertLabeledWebMarkupContainerLabel("", path, labelText);
	}

	public void assertLabeledWebMarkupContainerLabel(String expectedMessage, String path, String labelText) {
		LabeledWebMarkupContainer component = (LabeledWebMarkupContainer) getComponentFromLastRenderedPage(path);
		assertThat(expectedMessage, component.getLabel().getObject(), is(labelText));
	}

}
