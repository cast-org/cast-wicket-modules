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
package org.cast.cwm.service;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;

import java.util.Date;

/*
 * Service Wrapper for CwmSession that we can inject and mock to isolate session stuff in tests.
 * 
 * TODO: This doesn't get all the methods up the inheritance hierarchy.  
 * We'll add any that we need to wrap for mocks.
 * 
 */
public interface ICwmSessionService {

	/**
	 * Number of seconds of inactivity before the session should be considered possibly stale,
	 * and the user warned (if possible) that it will timeout.
	 * In the default implementation, this is based on the cwm.sessionWarningTime configuration setting.
	 * After the warning, the session is still kept alive for {@link #getSessionTimeoutTime()} seconds.
	 * @return number of seconds.
	 */
	int getSessionWarningTime();

	/**
	 * Number of seconds of additional inactivity after the user is warned before a session should be closed.
 	 * In the default implementation, this is based on the cwm.sessionTimeoutTime configuration setting.
	 * This time is in addition to {@link #getSessionWarningTime()}.
	 * @return number of seconds
	 */
	int getSessionTimeoutTime();

	/**
	 * Number of seconds left before the user should be warned that this session is inactive and will expire.
	 * Based on {@link #getSessionWarningTime()} and the last registered activity in the session.
	 * @return number of seconds; may be negative if warning time has passed.
	 */
	int timeToExpiryWarning();

	/**
	 * Number of seconds left before this session should be terminated for inactivity.
	 * Only accurate if expiry warning has already been posted.
	 * Based on {@link #getSessionTimeoutTime()} and the last registered activity in the session.
	 * @return number of seconds; may be negative if expiry time has passed.
	 */
	int timeToTimeout();

	/**
	 * Update the last-activity field in the session to the given time.
	 * If a later time has already been recorded, this will have no effect.
	 * @param date The time when some activity is known to have occurred in the session.
	 */
	void registerActivity(Date date);

	/**
	 * Update the last-activity field in the session to the current time.
	 */
	void registerActivity();

	IModel<LoginSession> getLoginSessionModel();
	IModel<Site> getCurrentSiteModel();
	IModel<Period> getCurrentPeriodModel();
	void setCurrentPeriodModel (IModel<Period> model);
	Long getLoginSessionId();
	LoginSession getLoginSession();
	void setLoginSessionModel (IModel<LoginSession> model);
	IModel<User> createUserModel(User user);
	User getUser();
	IModel<User> getUserModel();
	boolean isSignedIn();
	boolean signIn(String username, String password, boolean setCookie);
	boolean signIn(String username, String password);
	void signOut();
}
