/*
 * Copyright 2011-2019 CAST, Inc.
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

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Application;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.figuration.hideable.FigurationModal;
import org.cast.cwm.figuration.hideable.FigurationModalBasicHeader;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;

/**
 * Dialog that warns the user that their session is going to expire soon.
 *
 * To use, add this and a button tied to it (needed for focus management) to any page that
 * is part of a logged-in session.
 *
 * In your base logged-in Page object:
 * <code><pre>
 * add(new SessionExpireWarningDialog("timeoutModal"));
 * add(new WebMarkupContainer("timeoutButton").add(new FigurationTriggerBehavior(timeoutModal)));
 * </pre></code>
 *
 * In the base logged-in Page HTML:
 * <code><pre>
 * &lt;button wicket:id="timeoutButton" class="d-none">Open/close timeout warning&lt;/button>
 * &lt;div wicket:id="timeoutModal">&lt;/div>
 * </pre></code>
 *
 * The timing of when this dialog pops up and how long it remains displayed is defined by CwmSessionService;
 * the default implementation there reads two configuration settings:
 * cwm.sessionWarningTime (time before warning) and cwm.sessionTimeoutTime (time to leave warning up before
 * logging the user out).
 *
 * @author jbrookover
 * @author bgoldowsky
 *
 */
@Slf4j
public class SessionExpireWarningDialog extends FigurationModal<Void> implements IHeaderContributor {

	protected static final PackageResourceReference JAVASCRIPT_REFERENCE
			= new PackageResourceReference(SessionExpireWarningDialog.class, "SessionExpireWarningDialog.js");

	protected static final String CHECK_EVENT_NAME = "session-expire-check";
	protected static final String REFRESH_EVENT_NAME = "session-expire-dialog-closed";
	
	@Inject
	private ICwmService cwmService;

	@Inject
	private ICwmSessionService cwmSessionService;
	
	@Inject
	private IEventService eventService;

	private AbstractDefaultAjaxBehavior inactiveBehavior;
	
	public SessionExpireWarningDialog(String id) {
		super(id);

		add(new FigurationModalBasicHeader("header", new ResourceModel("timeoutHeader")));

		add(new ActivityCheckBehavior());
		add(new SessionRefreshBehavior());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		// The Javascript makes use of Loglevel for debugging
		response.render(JavaScriptHeaderItem.forReference(cwmService.getLoglevelJavascriptResourceReference()));
		response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT_REFERENCE));
		response.render(OnDomReadyHeaderItem.forScript(getInitializationJavascript()));
	}

	protected String getInitializationJavascript() {
		// initialize the warning js by calling SessionExpireWarning.init with the following params
		//	id - this dialog's markup ID (for triggering JS events)
		//	nextCheckTime - time, in seconds, before client should call back to check status
		//  checkEventName - name of event that will be triggered to check in with server
		//  closeEventName - name of event that will be triggered when the warning dialog is closed by the user
		//  homePage - URL to redirect to on session timeout
		//  debug - whether debugging messages should be output to the javascript console

		return String.format("SessionExpireWarning.init('%s', %d, '%s', '%s', '%s');",
				this.getMarkupId(),
				cwmSessionService.getSessionWarningTime(),
				CHECK_EVENT_NAME,
				REFRESH_EVENT_NAME,
				urlFor(Application.get().getHomePage(), new PageParameters().set("expired", "true")));
	}

	protected String nextCheckJavascript(long seconds) {
		return String.format("SessionExpireWarning.setNextCheck(%d);", seconds);
	}

	private void onWarning(AjaxRequestTarget target) {
		log.debug("Sending inactive warning to user");
		target.appendJavaScript(nextCheckJavascript(cwmSessionService.timeToTimeout()));
		target.appendJavaScript("SessionExpireWarning.warning();");
	}

	/**
	 * Called when the user did not respond to warning in the designated time.  This
	 * is where you can logout and redirect.  By default, a logged in user is redirected
	 * to {@link CwmApplication#getSignInPageClass()} with parameter 'expired=true' and
	 * all others are directed to the homepage.
	 *
	 * @param target the ajax request target
	 */
	protected void onInactive(AjaxRequestTarget target) {
		log.info("User logged out due to inactivity");

		// We're signed in; force logout and redirect to login.
		LoginSession loginSession = cwmSessionService.getLoginSession();
		if (loginSession != null) {
			eventService.forceCloseLoginSession(loginSession, "[timeout]");
			cwmService.flushChanges();
			cwmSessionService.setLoginSessionModel(null);
			cwmSessionService.signOut();
		}
		// Tell client side to redirect to login page.
		target.appendJavaScript("SessionExpireWarning.expired();");
	}

	protected void onActive(AjaxRequestTarget target) {
		// Session is still active; set up next check time to be current warn time.
		target.appendJavaScript(nextCheckJavascript(cwmSessionService.timeToExpiryWarning()));
		// Close warning dialog if it happened to be open
		target.appendJavaScript("SessionExpireWarning.clearWarning();");
	}


	protected class ActivityCheckBehavior extends AjaxEventBehavior {

		public ActivityCheckBehavior() {
			super(CHECK_EVENT_NAME);
		}

		@Override
		protected void onEvent(AjaxRequestTarget target) {
			log.debug("ActivityCheckBehavior triggered");

			// Possible future extension: Read last known activity from request, store into session
			// This would be useful if we want to count pure client-side non-ajax user actions as "activity"
			// for keep-alive purposes.  Would require some code like the following; and see also updateAjaxAttrs below.
			//
			// String LKA = RequestCycle.get().getRequest().getQueryParameters()
			//		.getParameterValue("lastKnownActivity").toString();
			// log.debug("Last known activity: {}", LKA);
			// cwmSessionService.registerActivity(LKA);

			if (cwmSessionService.timeToTimeout() < 0) {
				onInactive(target);
			} else if (cwmSessionService.timeToExpiryWarning() < 0) {
				onWarning(target);
			} else {
				onActive(target);
			}
		}

		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			// In case server can't be reached, invoke a client-side handler rather than default error behavior.
			attributes.getAjaxCallListeners()
					.add(new AjaxCallListener()
							.onFailure("SessionExpireWarning.checkFailed(attrs, jqXHR, errorMessage, textStatus);"));

			// More code needed if we need additional client-side activity tracking
			// attributes.getDynamicExtraParameters().add(String.format("return { %s : %s }",
			//  "lastKnownActivity", "SessionExpireWarning.lastKnownActivity"));
		}

	}


	protected class SessionRefreshBehavior extends AjaxEventBehavior {

		public SessionRefreshBehavior() {
			super(REFRESH_EVENT_NAME);
		}

		@Override
		protected void onEvent(AjaxRequestTarget target) {
			log.debug("SessionRefreshBehavior triggered");

			cwmSessionService.registerActivity();
			target.appendJavaScript(nextCheckJavascript(cwmSessionService.timeToExpiryWarning()));
		}

	}


}