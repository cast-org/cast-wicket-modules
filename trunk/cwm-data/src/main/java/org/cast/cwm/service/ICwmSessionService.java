/*
 * Copyright 2011-2014 CAST, Inc.
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

/*
 * Service Wrapper for CwmSession that we can inject and mock to isolate session stuff in tests.
 * 
 * TODO: This doesn't get all the methods up the inheritance hierarchy.  
 * We'll add any that we need to wrap for mocks.
 * 
 */
public interface ICwmSessionService {

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
