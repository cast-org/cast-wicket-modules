package org.cast.cwm.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequestCycle;
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

	public void executeAjaxBehavior(String path, String value) {
		AbstractAjaxBehavior behavior = (AbstractAjaxBehavior) getComponentFromLastRenderedPage(
				path).getBehaviors().get(0);
		CharSequence url = behavior.getCallbackUrl(false);
		WebRequestCycle cycle = setupRequestAndResponse(true);
		getServletRequest().setRequestToRedirectString(url.toString());
		String[] ids = path.split(":");
		String id = ids[ids.length - 1];
		getServletRequest().setParameter(id, value);
		processRequestCycle(cycle);
	}

	public void assertAttribute(String message, String expected,
			Component component, String attribute) {
		AttributeModifier behavior = getAttributeModifier(component, attribute);
		if (behavior != null) {
			try {
				IModel<?> model = (IModel<?>) getReplaceModelMethod
						.invoke(behavior);
				assertThat(message, model.getObject().toString(), equalTo("current"));
				return;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		fail("Attribute " + attribute + " not found.");
	}

	public void assertNotAttribute(String message, String expected,
			Component component, String attribute) {
		AttributeModifier behavior = getAttributeModifier(component, attribute);
		if (behavior == null) {
			return;
		}
		else {
			try {
				IModel<?> model = (IModel<?>) getReplaceModelMethod
						.invoke(behavior);
				assertThat(message, model.getObject().toString(), not(equalTo("current")));
				return;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	private AttributeModifier getAttributeModifier(Component component,
			String attribute) {
		List<IBehavior> behaviors = component.getBehaviors();
		for (IBehavior behavior : behaviors) {
			if (AttributeModifier.class.isAssignableFrom(behavior.getClass())) {
				AttributeModifier attributeModifier = (AttributeModifier) behavior;
				if (attribute.equals(attributeModifier.getAttribute()))
					return attributeModifier;
			}
		}
		return null;
	}

	public void assertErrorMessagesContain(String expectedMessage) {
		List<Serializable> actualMessages = this
				.getMessages(FeedbackMessage.ERROR);
		List<Serializable> msgs = new ArrayList<Serializable>();
		for (Iterator<Serializable> iterator = actualMessages.iterator(); iterator
				.hasNext();) {
			msgs.add(iterator.next().toString());
		}
		assertTrue("Messages do not contain expected string '"
				+ expectedMessage + "'", msgs.contains(expectedMessage));
	}

}
