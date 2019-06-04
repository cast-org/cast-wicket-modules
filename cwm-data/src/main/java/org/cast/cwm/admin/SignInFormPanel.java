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
package org.cast.cwm.admin;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.CwmSession;
import org.cast.cwm.components.BrowserInfoGatheringForm;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.FeedbackBorder;
import org.cast.cwm.db.service.IModelProvider;
import org.cast.cwm.service.IEventService;

/**
 * A simple Sign-In form for authenticating users.
 * Also will gather extended client info as part of the form submit; {@see BrowserInfoGatheringForm}
 * Usernames can be considered case-sensitive or not depending on {@link CwmApplication#usernamesCaseSensitive}.
 * Passwords are always case sensitive.
 *
 * @author jbrookover
 *
 */
@Slf4j
@StatelessComponent
public class SignInFormPanel extends Panel {

	private RequiredTextField<String> username;
	private PasswordTextField password;
	
	@Inject
	private IEventService eventService;

	@Inject
	private IModelProvider modelProvider;
	
	public SignInFormPanel(String id) {
		super(id);
		add(new SignInForm("form"));
	}

	public class SignInForm extends BrowserInfoGatheringForm<Void> {

		public SignInForm(String id) {
			super(id);

			add(new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this)));
			
			add((new FeedbackBorder("usernameBorder")).add(username = new RequiredTextField<>("username", new Model<String>())));
			add((new FeedbackBorder("passwordBorder")).add(password = new PasswordTextField("password", new Model<String>())));

			add(new FormComponentLabel("usernameLabel", username));
			add(new FormComponentLabel("passwordLabel", password));
		}
		
		@Override
		protected void onSubmit()	{
			super.onSubmit();

			CwmSession session = CwmSession.get();

			boolean loginSessionExists = false;

			String username = SignInFormPanel.this.username.getModelObject();
			if (getApplication() instanceof CwmApplication) {
				if (!((CwmApplication) getApplication()).isUsernamesCaseSensitive())
					username = username.toLowerCase();
			}
			if (session.isSignedIn()) {
				if (session.getUser() == null) {
					log.error("Session logged in as null user");
					session.signOut();
				} else if (session.getUser().getUsername().equals(username)) {
					log.warn("Already logged in as same user; ignoring login attempt!");
					loginSessionExists = true;
				} else {
					log.warn("Session was logged in as a different user, signing out before signing in...");
					session.signOut();
				}
			}

			if (!session.signIn(username, password.getModelObject())) {
				log.warn("Login failed, username {}", username);
				error(getLocalizer().getString("signInFailed", this, "Invalid username and/or password."));
				if (loginSessionExists)
					session.signOut();
				return;
			}

			getSession().bind();

			User user = session.getUser();
			if (!user.isValid()) {
				error(getLocalizer().getString("accountInvalid", this, "Account not confirmed.  You must confirm your account by clicking on the link in the email we sent you before you can log in."));
				return;
			}

			if (!loginSessionExists) {
				eventService.createLoginSession(getRequest());
				eventService.storeLoginEvent(this);
			}

			// Set current Period and Site in the session
			Period period = user.getDefaultPeriod();
			session.setCurrentPeriodModel(modelProvider.modelOf(period));
			if (period != null)
				session.setCurrentSiteModel(modelProvider.modelOf(period.getSite()));
			else
				session.setCurrentSiteModel(modelProvider.modelOf((Site)null));

			initialUserSetup(user);

			continueToOriginalDestination();
			// Normally continueToOriginalDestination doesn't return.  If it does, go to default page.
			setResponsePage(getDefaultPage(), new PageParameters().set("source", "login"));
		}
	}

	/**
	 * Do any needed housekeeping on the user at login time.
	 */
	protected void initialUserSetup(User user) {
		// nothing currently
	}

	/**
	 * Return the default page to load after login.
	 *
	 * Normally this is the application's home page.
	 * @return page class
	 */
	protected Class<? extends Page> getDefaultPage() {
		return Application.get().getHomePage();
	}

}
