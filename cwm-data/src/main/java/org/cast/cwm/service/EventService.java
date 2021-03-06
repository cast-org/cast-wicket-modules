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
package org.cast.cwm.service;

import com.google.inject.Inject;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.BasicCriteriaBuilder;
import net.databinder.models.hib.HibernateListModel;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Event;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.data.component.IEventDataContributor;
import org.cwm.db.service.IModelProvider;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Methods for working with Events and related information in the database.
 *
 */
public class EventService implements IEventService {
	
	public static final String LOGIN_TYPE_NAME = "login";
	public static final String LOGOUT_TYPE_NAME = "logout";
	public static final String TIMEOUT_TYPE_NAME = "logout:forced";
	public static final String PAGEVIEW_TYPE_NAME = "pageview";
	public static final String DIALOGVIEW_TYPE_NAME = "dialogview";
	public static final String POST_TYPE_NAME = "post";
	public static final String AUTOSAVE_POST_TYPE_NAME = "post:autosave";
	public static final String AGENTVIEW_TYPE_NAME = "agent:animate";
	
	private final static Logger log = LoggerFactory.getLogger(EventService.class);
	
	@Inject
	private ICwmService cwmService;

	@Inject
	private IModelProvider modelProvider;

	/** 
	 * Create a new Event instance.
	 * Applications that use a subclass of Event can override this factory method to create it.
	 */
	@Override
	public Event newEvent() {
		return new Event();
	}

	/**
	 * Save event to DB, after allowing all containing Components to embellish it.
	 * Before committing to database, each Component, starting with the Page
	 * and working down to the triggeringComponent, can add any relevant contextual
	 * information to the Event.  Any component that wants to add information should
	 * implement the {@link IEventDataContributor} interface.
	 *
	 * @param event the Event to be filled out and saved
	 * @param triggeringComponent the Component where the event was initiated (eg, a button)
	 * @return the persisted Event wrapped in an IModel
	 */
	public <T extends Event> IModel<T> storeEvent (T event, Component triggeringComponent) {
		gatherEventDataContributions(event, triggeringComponent);
		event.setComponentPath(triggeringComponent.getPageRelativePath());
		event.setDefaultValues();
		cwmService.save(event);
		cwmService.flushChanges();
		log.debug("Saved event: {}", event);
		return modelProvider.modelOf(event);
	}

	// Recursively collect information in the Event from all ancestors of the given Component.
	// The top-level Component (page) will contribute its data first, moving down the chain
	// to the triggering component last, so that more specific components can override values
	// set by more general ones.
	// Unchecked cast below - we assume that the Event will be of the correct subtype for each contributor.
	// Not sure how that could be avoided.
	@SuppressWarnings("unchecked")
	protected void gatherEventDataContributions (Event event, Component component) {
		if (component.getParent() != null)
			gatherEventDataContributions(event, component.getParent());
		if (component instanceof IEventDataContributor) {
			((IEventDataContributor) component).contributeEventData(event);
		}
	}

	/**
	 * Save an actual event object.  
	 * 
	 * @param e the Event to be saved
	 * @return model wrapping the event that was saved
	 */
	protected <T extends Event> IModel<T> saveEvent (T e) {
		e.setDefaultValues();
		Databinder.getHibernateSession().save(e);
		cwmService.flushChanges();
		log.debug("Event: {}: {}", e.getType(), e.getDetail());
		return modelProvider.modelOf(e);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#saveEvent(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public IModel<? extends Event> saveEvent(String type, String detail, String pageName, String componentPath) {
		Event e = newEvent();
		e.setType(type);
		e.setDetail(detail);
		e.setPage(pageName);
		e.setComponentPath(componentPath);
		return saveEvent(e);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#saveEvent(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public IModel<? extends Event> saveEvent(String type, String detail, String pageName) {
		return saveEvent(type, detail, pageName, null);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#saveLoginEvent()
	 */
	@Override
	public IModel<? extends Event> saveLoginEvent() {
		return saveEvent(LOGIN_TYPE_NAME, CwmSession.get().getUser().getRole().toString(), null, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#savePageViewEvent(java.lang.String, java.lang.String)
	 */
	@Override
	public IModel<? extends Event> savePageViewEvent (String detail, String pageName) {
		return saveEvent(PAGEVIEW_TYPE_NAME, detail, pageName);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#savePostEvent(boolean, java.lang.String)
	 */
	@Override
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
	@Override
	public LoginSession newLoginSession() {
		return new LoginSession();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#createLoginSession(org.apache.wicket.Request)
	 */
	@Override
	public LoginSession createLoginSession (Request r) {
		LoginSession loginSession = newLoginSession();
		CwmSession cwmSession = CwmSession.get();
		loginSession.setSessionId(cwmSession.getId());
		loginSession.setStartTime(new Date());
		loginSession.setUser(cwmSession.getUser());
		if (r instanceof ServletWebRequest) 
			loginSession.setIpAddress(((ServletWebRequest)r).getContainerRequest().getRemoteAddr());
		
		loginSession.setCookiesEnabled(false);
		if (cwmSession.getClientInfo() != null) {
			ClientProperties info = cwmSession.getClientInfo().getProperties();
			loginSession.setScreenHeight(info.getBrowserHeight());
			loginSession.setScreenWidth(info.getBrowserWidth());
			if (info.getTimeZone() != null)
				loginSession.setTimezoneOffset(info.getTimeZone().getOffset(new Date().getTime()));
			loginSession.setCookiesEnabled(info.isCookiesEnabled());
			loginSession.setPlatform(info.getNavigatorPlatform());
			loginSession.setUserAgent(cwmSession.getClientInfo().getUserAgent());
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
	@Override
	public void closeLoginSession (LoginSession loginSession, Date closeTime) {
		loginSession.setEndTime(closeTime);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#recordLogout()
	 */
	@Override
	public IModel<? extends Event> recordLogout() {
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
		return saveEvent(LOGOUT_TYPE_NAME, sesLength, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#getLoginSessionBySessionId(java.lang.String)
	 */
	@Override
	public IModel<LoginSession> getLoginSessionBySessionId(String httpSessionId) {
		return new HibernateObjectModel<LoginSession>(LoginSession.class,
				new BasicCriteriaBuilder(Restrictions.eq("sessionId", httpSessionId)));
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#forceCloseLoginSession(org.hibernate.Session, org.cast.cwm.data.LoginSession, java.lang.String)
	 */
	@Override
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
	@Override
	public IModel<List<String>> getEventTypes() {
		return new HibernateListModel<String>("select distinct type from Event", true);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#getLastEventTime(org.cast.cwm.data.LoginSession)
	 */
	@Override
	public Date getLastEventTime (LoginSession ls) {
		Session session = Databinder.getHibernateSession();
		Criteria criteria = session.createCriteria(Event.class);
		criteria.add(Restrictions.eq("loginSession", ls));
		criteria.setProjection(Projections.max("insertTime"));
		return (Date) criteria.uniqueResult();
	}
	
}
