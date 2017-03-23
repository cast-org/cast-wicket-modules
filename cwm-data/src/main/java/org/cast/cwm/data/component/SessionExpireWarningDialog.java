/*
 * Copyright 2011-2016 CAST, Inc.
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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.databinder.auth.AuthDataSessionBase;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.CwmSession;
import org.cast.cwm.figuration.component.FigurationModal;
import org.cast.cwm.figuration.component.FigurationModalBasicHeader;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.IEventService;

import java.util.Map;

/**
 * Dialog that warns the user that their session is going to expire soon.
 *
 * <p>To use, add this and a button tied to it (needed for focus management) to any page that
 * is part of a logged-in session.  Also, add a listener so that the Javascript knows when
 * AJAX requests are keeping the session active.</p>
 *
 * In Application.init():
 * <code><pre>
 * SessionExpireWarningDialog.addAjaxListener(this);
 * </pre></code>
 *
 * In your base logged-in Page object:
 * <code><pre>
 * add(new SessionExpireWarningDialog("timeoutModal"));
 * add(new WebMarkupContainer("timeoutButton").add(new ModalTriggerBehavior(timeoutModal)));
 * </pre></code>
 *
 * In the base logged-in Page HTML:
 * <code><pre>
 * &lt;button wicket:id="timeoutButton" class="d-none">Open/close timeout warning&lt;/button>
 * &lt;div wicket:id="timeoutModal">&lt;/div>
 * </pre></code>
 *
 * @author jbrookover
 * @author bgoldowsky
 *
 */
@Slf4j
public class SessionExpireWarningDialog extends FigurationModal<Void> implements IHeaderContributor {

	@Getter @Setter
	private boolean debug = false;

	// Note that the session timeout is set in CwmApplication - sessionTimeout

	@Getter @Setter
	private int warningTime = 60 * 5; // Number of seconds before session expires that the user receives a warning.

	@Getter @Setter
	private int responseTime = 60 * 4; // Number of seconds after warning before the user is automatically logged out.

	protected static final PackageResourceReference JAVASCRIPT_REFERENCE
			= new PackageResourceReference(SessionExpireWarningDialog.class, "SessionExpireWarningDialog.js");
	
	@Inject
	private ICwmService cwmService;
	
	@Inject
	private IEventService eventService;

	private AbstractDefaultAjaxBehavior inactiveBehavior;
	
	public SessionExpireWarningDialog(String id) {
		super(id);

		add(new FigurationModalBasicHeader("header", new ResourceModel("timeoutHeader")));

		inactiveBehavior = new AbstractDefaultAjaxBehavior() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void respond(AjaxRequestTarget target) {
				onInactive(target);
			}
		};
		add(inactiveBehavior);
	}
	
	/**
	 * Called when the user indicates that they are still working.
	 * 
	 * @param target Ajax request target
	 */
	protected void keepAliveCall(AjaxRequestTarget target) {
		target.appendJavaScript(getResetJavascript());
	}
	
	/**
	 * Called when the user did not respond to warning in the designated time.  This
	 * is where you can logout and redirect.  By default, a logged in user is redirected
	 * to {@link CwmApplication#getSignInPageClass()} with parameter 'expired=true' and 
	 * all others are directed to the homepage.
	 * 
	 * <p><strong>Note</strong>: In a proper setup, this call itself will refresh
	 * the HttpSession.  So, if no action is taken, the mere presence of this component
	 * will keep the session alive.</p>
	 *
	 * @param target the ajax request target
	 */
	protected void onInactive(AjaxRequestTarget target) {
		log.info("Inactive User Detected");
		
		// We're signed in; redirect to login.
		if (CwmSession.get().getLoginSession() != null) {
			eventService.forceCloseLoginSession(CwmSession.get().getLoginSession(), "[timeout]");
			cwmService.flushChanges();
			CwmSession.get().setLoginSessionModel(null);
			AuthDataSessionBase.get().signOut();
			setResponsePage(CwmApplication.get().getSignInPageClass(), new PageParameters().set("expired", "true"));
		
		// We're not signed in; redirect to home because page is no longer functional
		} else {
			setResponsePage(CwmApplication.get().getHomePage());
		}			
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		if (warningTime <= responseTime) {
			throw new IllegalStateException("Warning time must be greater than response time.");
		}

		if (warningTime >= CwmApplication.get().getSessionTimeout()) {
			throw new IllegalStateException("Warning time must be less than session time.");
		}

		response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT_REFERENCE));

		StringBuilder script = new StringBuilder();
		script.append(getInitializationJavascript());
		script.append(getUserResponseWatcherJavascript());
		if (debug)
			script.append(getDebugJavascript());

		response.render(OnDomReadyHeaderItem.forScript(script.toString()));
	}

	protected String getInitializationJavascript() {
		// initialize the warning js by calling SessionExpireWarning.init with the following params
		//	sessionLength - length, in seconds, of the HttpSession
		//	warningTime - time, in seconds, before HttpSession ends to trigger a warning
		//	warningCallbackFunction - function that is triggered to warn the user of impending session expiration
		//	logoutDelay - time, in seconds, the user has to respond to the warning
		//	inactiveCallbackFunction - function that is triggered if the user does not respond to warning

		String script = "SessionExpireWarning.init(" +
				CwmApplication.get().getSessionTimeout() + ", " +
				warningTime + ", " +
				"function() {" + getCommandJavascript("show") + "}, " +
				responseTime + ", " +
				"function() { " +
				getBeforeInactiveTimeoutJavaScript() +
				"Wicket.Ajax.get({u:'" + inactiveBehavior.getCallbackUrl() + "'}); " +
				"});";
		return script;
	}

	protected String getUserResponseWatcherJavascript() {
		return String.format("$('#%s').on('afterHide.cfw.modal', SessionExpireWarning.reset);",
				this.getMarkupId());
	}

	protected String getDebugJavascript() {
		return "SessionExpireWarning.DEBUG = true;";
	}
	
	protected String getBeforeInactiveTimeoutJavaScript() {
		// Subclasses can use this to execute JavaScript just before the seeion close callback.
		return "";
	}

	/**
	 * Returns a javascript that will reset the timer.
	 * Used after AJAX requests, see {@link #addAjaxListener(WebApplication)}
	 *
	 * @return Javascript string.
	 */
	public static String getResetJavascript() {
		return "if (typeof SessionExpireWarning != \"undefined\" && typeof SessionExpireWarning.reset == \"function\") { SessionExpireWarning.reset(); }";
	}

	/**
	 * Add the appropriate AJAX request listener so that the session expiry warning is aware of AJAX traffic.
	 * Sessions that are actively making AJAX requests should not be considered idle, and user
	 * does not need to be warned about expiry.
	 *
	 * All applications that use the SessinExpireWarningDialog should call this method from Application.init().
	 *
	 * @param application the wicket application object
	 */
	public static void addAjaxListener(WebApplication application) {
		application.getAjaxRequestTargetListeners().add(new SessionAliveListener());
	}


	/**
	 * Ajax request target listener that resets session expiry timer on every AJAX request,
	 * so that a session that is actively making AJAX requests is not considered inactive
	 * and user is not warned about expiry.
	 *
	 * @see #addAjaxListener(WebApplication)
	 */
	public static class SessionAliveListener extends AjaxRequestTarget.AbstractListener {

		@Override
		public void onAfterRespond(Map<String, Component > map, AjaxRequestTarget.IJavaScriptResponse response) {
			response.addJavaScript(getResetJavascript());
		}

	}
}