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
package org.cast.cwm.data.init;

import java.util.Date;
import java.util.Properties;

import net.databinder.models.hib.HibernateListModel;

import org.apache.wicket.injection.web.InjectorHolder;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.data.builders.LoginSessionCriteriaBuilder;
import org.cast.cwm.service.IEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Database initializer that will close any open LoginSession objects that have expired.
 * 
 * App Properties expected:
 *  none, but will ask the application for its session timeout value, which can be specified with the cwm.sessionTimeout.
 * 
 * @author bgoldowsky
 *
 */
public class CloseOldLoginSessions implements IDatabaseInitializer {
	
	private static final Logger log = LoggerFactory.getLogger(CloseOldLoginSessions.class);
	
	@Inject
	private IEventService eventService;
	
	public CloseOldLoginSessions() {
		super();
		InjectorHolder.getInjector().inject(this);
	}

	public String getName() {
		return "close old loginsessions";
	}

	public boolean isOneTimeOnly() {
		return false;
	}

	public boolean run(Properties appProperties) {
		boolean changesMade = false;
		Date now = new Date();
		long expiryTime = now.getTime() - CwmApplication.get().getSessionTimeout()*1000;
		for (LoginSession ls : new HibernateListModel<LoginSession>(LoginSession.class, new LoginSessionCriteriaBuilder()).getObject()) {
			Date lastEventTime = eventService.getLastEventTime(ls);
			if (lastEventTime == null || lastEventTime.getTime()<expiryTime) {
				log.debug("Closing stale LoginSession: {}", ls);
				eventService.closeLoginSession(ls, lastEventTime==null ? new Date() : lastEventTime);
				changesMade = true;
			} else {
				log.debug("LoginSession {} is still alive as of {}", ls, lastEventTime);
			}
		}
		return changesMade;
	}

}
