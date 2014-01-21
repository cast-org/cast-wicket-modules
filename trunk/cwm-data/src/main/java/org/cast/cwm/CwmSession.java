/*
 * Copyright 2011-2013 CAST, Inc.
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
import net.databinder.auth.AuthDataSessionBase;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Session;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.service.IEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Extension of Databinder's AuthDataSession that keeps track of the LoginSession.
 * 
 * @author bgoldowsky
 *
 */
public class CwmSession extends AuthDataSessionBase<User> {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(CwmSession.class);

	@Getter
	private IModel<LoginSession> loginSessionModel;
	
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
	
	// Would not ordinarily keep track of this separately, but CwmSessionStore needs it & can't trust HibnerateObjectModel
	@Getter
	private Long loginSessionId;
	
	@Inject
	IEventService eventService;

	public CwmSession(Request request) {
		super(request);
		Injector.get().inject(this);
	}
	
	public static CwmSession get() {
		return (CwmSession) Session.get();
	}
	
	@Override
	public void signOut() {
		synchronized(this) {
			if (loginSessionModel != null) {
				eventService.recordLogout();
				loginSessionModel = null;
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
		return new HibernateObjectModel<User>(user);
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
