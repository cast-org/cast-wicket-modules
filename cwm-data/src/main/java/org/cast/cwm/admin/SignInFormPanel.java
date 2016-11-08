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
package org.cast.cwm.admin;

import net.databinder.models.hib.HibernateObjectModel;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.FeedbackBorder;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.ISiteService;
import org.cast.cwm.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import java.util.List;

/**
 * A simple Sign-In form for authenticating users.
 * 
 * @author jbrookover
 *
 */
public class SignInFormPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(SignInForm.class);
	
	private RequiredTextField<String> username;
	private PasswordTextField password;
	
	@Inject
	protected IEventService eventService;

	@Inject 
	protected ISiteService siteService;
	
	public SignInFormPanel(String id) {
		super(id);
		add(new SignInForm("form"));
	}

	public class SignInForm extends Form<User> {
		
		private static final long serialVersionUID = 1L;
		
		public SignInForm(String id) {
			super(id);
			
			add(new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this)));
			
			add((new FeedbackBorder("usernameBorder")).add(username = new RequiredTextField<String>("username", new Model<String>())));
			add((new FeedbackBorder("passwordBorder")).add(password = new PasswordTextField("password", new Model<String>())));
			
			add(new FormComponentLabel("usernameLabel", username));
			add(new FormComponentLabel("passwordLabel", password));
		}
		
		@Override
		protected void onSubmit()	{
			CwmSession session = CwmSession.get();

			boolean loginSessionExists = false;

			if (session.isSignedIn()) {
				if (session.getUser().getUsername().equals(username.getModelObject())) {
					log.warn("Already logged in as same user; ignoring login attempt!");
					loginSessionExists = true;
				} else {
					log.warn("Session was logged in as a different user, signing out before signing in...");
					session.signOut();
				}
			}

			if (!session.signIn(username.getModelObject(), password.getModelObject())) {
				log.warn("Login failed, username {}", username.getModelObject());
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
				eventService.saveLoginEvent(this);
			}

			List<Period> plist = user.getPeriodsAsList();
			if (plist != null && !plist.isEmpty()) {
				session.setCurrentPeriodModel(new HibernateObjectModel<Period>(plist.get(0)));
				session.setCurrentSiteModel(new HibernateObjectModel<Site>(plist.get(0).getSite()));
			} else {
				IModel<Period> mPeriod = siteService.getPeriodByName("test");
				if (user.hasRole(Role.RESEARCHER) && mPeriod.getObject() != null) {
					session.setCurrentPeriodModel(mPeriod);
					session.setCurrentSiteModel(new HibernateObjectModel<Site>(mPeriod.getObject().getSite()));
				} else {
					log.error("There must be a period named 'test' to use as the admin user's default period.");
					error(getLocalizer().getString("signInFailed", this, "User has no classroom to log in to"));
					return;
				}
			}

			initialUserSetup(user);

		}
	}

	/**
	 * Do any needed housekeeping on the user at login time.
	 */
	private void initialUserSetup(User user) {
		// nothing currently
	}

}
