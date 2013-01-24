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
package org.cast.cwm.service;

import java.util.Date;
import java.util.List;

import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateListModel;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Event;
import org.cast.cwm.data.LoginSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Methods for working with Events and related information in the database.
 *
 */
public class EventService implements IEventService {
	
	public static final String LOGIN_TYPE_NAME = "login";
	public static final String LOGOUT_TYPE_NAME = "logout";
	public static final String TIMEOUT_TYPE_NAME = "logout:forced";
	public static final String PAGEVIEW_TYPE_NAME = "pageview";
	public static final String POST_TYPE_NAME = "post";
	public static final String AUTOSAVE_POST_TYPE_NAME = "post:autosave";
	public static final String AGENTVIEW_TYPE_NAME = "agent:animate";
	
	private final static Logger log = LoggerFactory.getLogger(EventService.class);
	
	@Inject
	private ICwmService cwmService;

	/** 
	 * Create a new Event instance.
	 * Applications that use a subclass of Event can override this factory method to create it.
	 */
	public Event newEvent() {
		return new Event();
	}
	
	/**
	 * Save an actual event object.  Private and final because I feel
	 * everyone should call {@link #saveEvent(String, String, String)}.
	 * 
	 * @param e
	 * @return model wrapping the event that was saved
	 */
	protected IModel<? extends Event> saveEvent (Event e) {
		e.setDefaultValues();
		Databinder.getHibernateSession().save(e);
		cwmService.flushChanges();
		log.debug("Event: {}: {}", e.getType(), e.getDetail());
		return new HibernateObjectModel<Event>(e);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#saveEvent(java.lang.String, java.lang.String, java.lang.String)
	 */
	public IModel<? extends Event> saveEvent(String type, String detail, String pageName) {
		Event e = newEvent();
		e.setType(type);
		e.setDetail(detail);
		e.setPage(pageName);
		return saveEvent(e);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#saveLoginEvent()
	 */
	public void saveLoginEvent() {
		saveEvent(LOGIN_TYPE_NAME, CwmSession.get().getUser().getRole().toString(), null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#savePageViewEvent(java.lang.String, java.lang.String)
	 */
	public void savePageViewEvent (String detail, String pageName) {
		saveEvent(PAGEVIEW_TYPE_NAME, detail, pageName);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#savePostEvent(boolean, java.lang.String)
	 */
	public IModel<? extends Event> savePostEvent(boolean hasResponses, String pageName) {
		Event e = newEvent();
		e.setType(RequestCycle.get().getRequest().getRequestParameters().getParameterValue("autosave").toBoolean() ? AUTOSAVE_POST_TYPE_NAME : POST_TYPE_NAME);
		e.setHasResponses(hasResponses);
		e.setPage(pageName);
		return saveEvent(e);
	}
	
	////// Login Session methods
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#newLoginSession()
	 */
	public LoginSession newLoginSession() {
		return new LoginSession();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#createLoginSession(org.apache.wicket.Request)
	 */
	public LoginSession createLoginSession (Request r) {
		LoginSession loginSession = newLoginSession();
		CwmSession cwmSession = CwmSession.get();
		loginSession.setSessionId(cwmSession.getId());
		loginSession.setStartTime(new Date());
		loginSession.setUser(cwmSession.getUser());
		if (r instanceof ServletWebRequest) 
			loginSession.setIpAddress(((ServletWebRequest)r).getContainerRequest().getRemoteAddr());
		
		loginSession.setCookiesEnabled(false);
		if (cwmSession.getClientInfo() instanceof WebClientInfo) {
			ClientProperties info = ((WebClientInfo) cwmSession.getClientInfo()).getProperties();
			loginSession.setScreenHeight(info.getBrowserHeight());
			loginSession.setScreenWidth(info.getBrowserWidth());
			if (info.getTimeZone() != null)
				loginSession.setTimezoneOffset(info.getTimeZone().getOffset(new Date().getTime()));
			loginSession.setCookiesEnabled(info.isCookiesEnabled());
			loginSession.setPlatform(info.getNavigatorPlatform());
			loginSession.setUserAgent(((WebClientInfo)cwmSession.getClientInfo()).getUserAgent());
			// TODO
			//loginSession.setflashVersion(flashVersion)
			// isJavaEnabled
		}
		Databinder.getHibernateSession().save(loginSession);

		cwmService.flushChanges();
		
		// register loginSession with Wicket session
		CwmSession.get().setLoginSessionModel(new HibernateObjectModel<LoginSession>(loginSession));
		
		return loginSession;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#closeLoginSession(org.cast.cwm.data.LoginSession, java.util.Date)
	 */
	public void closeLoginSession (LoginSession loginSession, Date closeTime) {
		loginSession.setEndTime(closeTime);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#recordLogout()
	 */
	public void recordLogout() {
		LoginSession ls = CwmSession.get().getLoginSession();

		String sesLength = "";
		if (ls != null) {
			log.debug("Logout user {}", ls.getUser().getUsername());
			Date now = new Date();
			closeLoginSession(ls, now);
			cwmService.flushChanges();
			
			sesLength = "Session length=" + (now.getTime()-ls.getStartTime().getTime())/1000 + "s";
			
		} else {
			log.debug ("recordLogout found no LoginSession");
		}
		// saveEvent will commit the transaction
		saveEvent(LOGOUT_TYPE_NAME, sesLength, null);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#forceCloseLoginSession(org.hibernate.Session, org.cast.cwm.data.LoginSession, java.lang.String)
	 */
	public void forceCloseLoginSession(LoginSession loginSession, String comment) {
		Date now = new Date();
		closeLoginSession(loginSession, now); // TODO: consider setting this to date of last Event

		// Record Event for the session end
		Event ev = newEvent();
		ev.setType(TIMEOUT_TYPE_NAME);
		ev.setDetail("Session length=" + (now.getTime()-loginSession.getStartTime().getTime())/1000 + "s " + comment);
		ev.setInsertTime(now);
		ev.setLoginSession(loginSession);
		ev.setUser(loginSession.getUser());
		saveEvent(ev);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#getEventTypes()
	 */
	public IModel<List<String>> getEventTypes() {
		return new HibernateListModel<String>("select distinct type from Event", true);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#getLastEventTime(org.cast.cwm.data.LoginSession)
	 */
	public Date getLastEventTime (LoginSession ls) {
		Session session = Databinder.getHibernateSession();
		Criteria criteria = session.createCriteria(Event.class);
		criteria.add(Restrictions.eq("loginSession", ls));
		criteria.setProjection(Projections.max("insertTime"));
		return (Date) criteria.uniqueResult();
	}
	
}
