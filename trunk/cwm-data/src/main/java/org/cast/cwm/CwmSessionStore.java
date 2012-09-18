/*
 * Copyright 2011 CAST, Inc.
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

import java.io.Serializable;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import net.databinder.hib.DataApplication;

import org.apache.wicket.Application;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.service.IEventService;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/** 
 * A session store that adds a listener to do cleanup of CwmSession objects.
 * 
 * Also watches the session size and warns about it if it is too large.
 * 
 * @author bgoldowsky
 */
public class CwmSessionStore extends SecondLevelCacheSessionStore {

	private static final long SESSION_WARN_LIMIT = 500000; // In Bytes
	
	private CwmApplication application;
	private static final Logger log = LoggerFactory.getLogger(CwmSessionStore.class);

	public CwmSessionStore(CwmApplication application, IPageStore pageStore) {
		super(application, pageStore);
		this.application = application;
	}

	/**
	 * As a new CwmSession is created, this attaches a listener in the servlet context
	 * that can take action later when the session is destroyed.
	 */
	@Override
	protected void onBind(Request request, Session newSession) {
		super.onBind(request, newSession);
		if (newSession instanceof CwmSession) {
			CwmSession session = (CwmSession) newSession;
			HttpSession httpSession = getHttpSession(toWebRequest(request));
			log.debug("Adding listener to new session {}", httpSession.getId());
			httpSession.setMaxInactiveInterval(application.getSessionTimeout()); // in seconds
			httpSession.setAttribute("Cwm:SessionUnbindingListener-" + application.getApplicationKey(),
					new CwmSessionBindingListener(session));
		}
	}

	
	/**
	 * This is the listener attached to each session.
	 * It reacts on unbinding from the session by cleaning up the session related application data.
	 */
	protected static final class CwmSessionBindingListener implements HttpSessionBindingListener, Serializable {
		
		private final CwmSession cwmSession;
		private final String appKey;

		// FIXME Injection doesn't work here since we're not in the application's thread
//		@Inject
//		private IEventService eventService;

		private static final long serialVersionUID = 1L;

		public CwmSessionBindingListener(CwmSession session) {
			this.cwmSession = session;
			this.appKey = Application.get().getApplicationKey();
			InjectorHolder.getInjector().inject(this);
		}

		/**
		 * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
		 */
		public void valueBound(HttpSessionBindingEvent evg)	{
		}

		/**
		 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
		 */
		public void valueUnbound(HttpSessionBindingEvent evt) {
			if (cwmSession == null) {
				log.debug("Unbinding null cwmSession");
				return;
			}
			Long lsid = cwmSession.getLoginSessionId();
			if (lsid != null) {
				CwmApplication app = (CwmApplication) CwmApplication.get(appKey); 
				app.expireLoginSession(lsid);
			} else {
				log.debug ("Session {} destroyed; no LoginSession", evt.getSession().getId());
			}
		}

	}

	@Override
	public void setAttribute(Request request, String name, Object value) {
		super.setAttribute(request, name, value);

		if (Session.get().getSizeInBytes() > SESSION_WARN_LIMIT) {
			log.warn("Session Size is {} bytes", Session.get().getSizeInBytes());
		}
	}
}
