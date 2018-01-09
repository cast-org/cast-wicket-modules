/*
 * Copyright 2011-2018 CAST, Inc.
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
package org.cast.cwm;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.databinder.auth.AuthDataSessionBase;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;
import org.cwm.db.service.IModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import java.util.Date;

/**
 * Extension of Databinder's AuthDataSession that keeps track of the LoginSession.
 * 
 * @author bgoldowsky
 *
 */
@Slf4j
public class CwmSession extends AuthDataSessionBase<User> {

	/**
	 * The database representation of this session (if logged in)
	 */
	@Getter
	private IModel<LoginSession> loginSessionModel;

	// Would not ordinarily keep track of this separately, but there may be situations
	// where you can't trust HibnerateObjectModel  (TODO not sure if this still applies)
	@Getter
	private Long loginSessionId;

	/**
	 * Most recent time that we know there was activity in this session.
	 * This is used to determine when the user should be warned, and then automatically logged out.
	 */
	@Getter
	private Date lastKnownActivity = new Date();

	/** Can be used to hold one of the Sites that the user is connected to,
	 * this would be considered their "current" site.
	 */
	@Getter @Setter
	private IModel<Site> currentSiteModel;
	
	/** Can be used to hold one of the Periods that the user is connected to.
	 * This would be considered their "current" Period (or may be their only one).
	 */
	@Getter @Setter
	private IModel<Period> currentPeriodModel;
	
	@Inject
	private IEventService eventService;

	@Inject
	private IModelProvider modelProvider;

	public CwmSession(Request request) {
		super(request);
		Injector.get().inject(this);
		registerActivity();
	}
	
	public static CwmSession get() {
		return (CwmSession) Session.get();
	}

	/**
	 * Let the session know that some activity has just happened (eg, an AJAX operation).
	 * Call whenever there is activity in the session that should re-start the counter to session expiry.
	 */
	public void registerActivity() {
		registerActivity(new Date());
	}

	/**
	 * Let the session know that some activity happened at a given time.
	 * If this is later than the currently last-known activity, it will update that information.
	 * @param date Date of some activity in the session
	 */
	public void registerActivity(Date date) {
		if (date.after(lastKnownActivity)) {
			lastKnownActivity = date;
		}
	}

	@Override
	public void signOut() {
		signOut(null);
	}

	public void signOut(Component triggerComponent) {
		synchronized(this) {
			if (loginSessionModel != null) {
				// Normally shouldn't be null, but can happen with database changes & restarts.
				if (getUser() != null)
					eventService.recordLogout(triggerComponent);
				loginSessionModel = null;
				loginSessionId = null;
			}
		}
		super.signOut();
	}

	public LoginSession getLoginSession() {
		if (loginSessionModel == null)
			return null;
		return loginSessionModel.getObject();
	}
	
	// Nonstandard setter, responsible for keeping loginSessionId also up to date.
	public void setLoginSessionModel (IModel<LoginSession> model) {
		loginSessionModel = model;
		if (model != null && model.getObject() != null)
			loginSessionId = model.getObject().getId();
		else
			loginSessionId = null;
	}

	@Override
	public IModel<User> createUserModel(User user) {
		return modelProvider.modelOf(user);
	}
	
	@Override
	public void detach() {
		if (loginSessionModel != null)
			loginSessionModel.detach();
		if (currentPeriodModel != null)
			currentPeriodModel.detach();
		if (currentSiteModel != null)
			currentSiteModel.detach();
		super.detach();
	}
	
}
