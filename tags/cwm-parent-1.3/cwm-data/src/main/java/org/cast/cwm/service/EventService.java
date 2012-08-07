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
package org.cast.cwm.service;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateListModel;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.cast.cwm.AbstractEventService;
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
public class EventService extends AbstractEventService {
	
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

	@Getter @Setter  
	protected Class<? extends Event> eventClass = Event.class;
	
	@Getter @Setter
	protected Class<? extends LoginSession> loginSessionClass = LoginSession.class;

	public EventService() {
		InjectorHolder.getInjector().inject(this);
	}
	
	public static EventService get() {
		return (EventService)instance;
	}
	
	public static void setInstance(EventService instance) {
		AbstractEventService.instance = instance;
	}
	
	/** 
	 * Create a new Event of the proper subclass type.
	 */
	public final Event newEvent() {
		try {
			return eventClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}	
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
	
	/**
	 * Save an event to the datastore.
	 * 
	 * @see {@link AbstractEventService#saveEvent(String, String, String)}
	 * @return model wrapping the event that was saved
	 */
	public IModel<? extends Event> saveEvent(String type, String detail, String pageName) {
		Event e = newEvent();
		e.setType(type);
		e.setDetail(detail);
		e.setPage(pageName);
		return saveEvent(e);
	}

	/**
	 * Save a login event.
	 * 
	 */
	public void saveLoginEvent() {
		saveEvent(LOGIN_TYPE_NAME, CwmSession.get().getUser().getRole().toString(), null);
	}
	
	/**
	 * Save a page view event.
	 * 
	 * @param detail
	 * @param pageName
	 */
	public void savePageViewEvent (String detail, String pageName) {
		saveEvent(PAGEVIEW_TYPE_NAME, detail, pageName);
	}
	
	/**
	 * Save a post event.  If this event has an accompanying {@link ResponseData} object,
	 * set hasResponses=true so the event log knows to look it up.
	 * 
	 * @param hasResponses 
	 * @return
	 */
	public IModel<? extends Event> savePostEvent(boolean hasResponses, String pageName) {
		Event e = newEvent();
		e.setType(Boolean.valueOf(RequestCycle.get().getRequest().getParameter("autosave")) ? AUTOSAVE_POST_TYPE_NAME : POST_TYPE_NAME);
		e.setHasResponses(hasResponses);
		e.setPage(pageName);
		return saveEvent(e);
	}
	/** 
	 * Create a LoginSession object in the database based on the current Session and the given Request.
	 * This should generally be called when a user logs in.
	 * Many details about the client and session will be filled in automatically if you put this in your 
	 * Application's init() method:
	 *   getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
	 * (see the javadoc for ClientProperties).	
	 * @param r the current Request object
	 * @return the created LoginSession
	 */
	public LoginSession createLoginSession (Request r) {
		LoginSession loginSession;
		try {
			loginSession = loginSessionClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		CwmSession s = CwmSession.get();
		loginSession.setSessionId(s.getId());
		loginSession.setStartTime(new Date());
		loginSession.setUser(s.getUser());
		if (r instanceof ServletWebRequest) 
			loginSession.setIpAddress(((ServletWebRequest)r).getHttpServletRequest().getRemoteAddr());
		
		loginSession.setCookiesEnabled(false);
		if (s.getClientInfo() instanceof WebClientInfo) {
			ClientProperties info = ((WebClientInfo) s.getClientInfo()).getProperties();
			loginSession.setScreenHeight(info.getBrowserHeight());
			loginSession.setScreenWidth(info.getBrowserWidth());
			if (info.getTimeZone() != null)
				loginSession.setTimezoneOffset(info.getTimeZone().getOffset(new Date().getTime()));
			loginSession.setCookiesEnabled(info.isCookiesEnabled());
			loginSession.setPlatform(info.getNavigatorPlatform());
			loginSession.setUserAgent(((WebClientInfo)s.getClientInfo()).getUserAgent());
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

	/**
	 * Close the current {@link LoginSession} and save a Logout Event
	 */
	public void recordLogout() {
		LoginSession ls = CwmSession.get().getLoginSession();

		String sesLength = "";
		if (ls != null) {
			log.debug("Logout user {}", ls.getUser().getUsername());
			Date now = new Date();
			ls.setEndTime(now);
			Databinder.getHibernateSession().update(ls);
			cwmService.flushChanges();
			
			sesLength = "Session length=" + (now.getTime()-ls.getStartTime().getTime())/1000 + "s";
			
		} else {
			log.debug ("recordLogout found no LoginSession");
		}
		// saveEvent will commit the transaction
		saveEvent(LOGOUT_TYPE_NAME, sesLength, null);
	}

	/**
	 * 
	 * Closes a LoginSession without a logout - timeout or server shutdown. This will
	 * also save an event.  
	 * <p>
	 * <b>Note</b>: Since this is called from shutdown hooks, a regular Databinder 
	 * session may not be available.  Therefore, a Databinder session must be passed in.  
	 * This method will NOT commit any changes or handle any transactions.  The session
	 * transaction must be flushed after this method is called.   
	 * </p>
	 * 
	 * @param dbSession a Hibernate session to use
	 * @param loginSession the LoginSession to be closed
	 * @param comment added to the event detail field
	 */
	public void forceCloseLoginSession(Session dbSession, LoginSession loginSession, String comment) {
		Date now = new Date();
		loginSession.setEndTime(now); // TODO: consider setting this to date of last Event

		// Record Event for the session end
		Event ev = newEvent();
		ev.setType(TIMEOUT_TYPE_NAME);
		ev.setDetail("Session length=" + (now.getTime()-loginSession.getStartTime().getTime())/1000 + "s " + comment);
		ev.setInsertTime(now);
		ev.setLoginSession(loginSession);
		ev.setUser(loginSession.getUser());
		dbSession.save(ev);
	}

	/**
	 * Return a list of all types of events currently found in the database.
	 * Used for creating filters in Event Log page.
	 * @return
	 */
	public IModel<List<String>> getEventTypes() {
		return new HibernateListModel<String>("select distinct type from Event", true);
	}
	
	/**
	 * Get the date of most recent event in a LoginSession.  
	 * Use sparingly; this requires a database query across all events in the LoginSession.
	 */
	public Date getLastEventTime (LoginSession ls) {
		Session session = Databinder.getHibernateSession();
		Criteria criteria = session.createCriteria(Event.class);
		criteria.add(Restrictions.eq("loginSession", ls));
		criteria.setProjection(Projections.max("insertTime"));
		return (Date) criteria.uniqueResult();
	}
	
}
