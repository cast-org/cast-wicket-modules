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
import org.cast.cwm.CwmSession;
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
public class CwmSessionService implements ICwmSessionService {

	public IModel<LoginSession> getLoginSessionModel() {
		return CwmSession.get().getLoginSessionModel();
	}

	public IModel<Site> getCurrentSiteModel() {
		return CwmSession.get().getCurrentSiteModel();
	}

	public IModel<Period> getCurrentPeriodModel() {
		return CwmSession.get().getCurrentPeriodModel();
	}

	public Long getLoginSessionId() {
		return CwmSession.get().getLoginSessionId();
	}

	public LoginSession getLoginSession() {
		return CwmSession.get().getLoginSession();
	}

	public void setLoginSessionModel(IModel<LoginSession> model) {
		CwmSession.get().setLoginSessionModel(model);
	}

	public IModel<User> createUserModel(User user) {
		return CwmSession.get().createUserModel(user);
	}

	public User getUser() {
		return CwmSession.get().getUser();
	}

	public IModel<User> getUserModel() {
		return CwmSession.get().getUserModel();
	}

	public boolean isSignedIn() {
		return CwmSession.get().isSignedIn();
	}

	public boolean signIn(String username, String password) {
		return CwmSession.get().signIn(username, password);
	}

	public boolean signIn(String username, String password, boolean setCookie) {
		return CwmSession.get().signIn(username, password, setCookie);
	}

	public void signOut() {
		CwmSession.get().signOut();
	}

	public void setCurrentPeriodModel(IModel<Period> model) {
		CwmSession.get().setCurrentPeriodModel(model);
	}

}
