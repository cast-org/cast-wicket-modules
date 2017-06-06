/*
 * Copyright 2011-2017 CAST, Inc.
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
import lombok.extern.slf4j.Slf4j;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.BasicCriteriaBuilder;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.cast.cwm.CwmSession;
import org.cast.cwm.IEventType;
import org.cast.cwm.data.Event;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.data.component.IEventDataContributor;
import org.cwm.db.service.IModelProvider;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.Date;

/**
 * Default implementations of various methods for working with Events and related information in the database.
 *
 * Applications must override this and implement a few methods that specify details of how they will handle events.
 *
 */
@Slf4j
public abstract class EventService implements IEventService {

	@Inject
	private ICwmService cwmService;

	@Inject
	private ICwmSessionService cwmSessionService;

	@Inject
	private IModelProvider modelProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Event newEvent() {
		return new Event();
	}

	/**
	 * @return event type that should be used for recording user logins
	 */
	protected abstract IEventType getLoginEventType();

	/**
	 * @return event type that should be used for recording intentional user logouts
	 */
	protected abstract IEventType getLogoutEventType();

	/**
	 * @return event type that should be used for recording automatic logouts due to a user's session timing out
	 */
	protected abstract IEventType getTimeoutEventType();

	/**
	 * @return event type used for recording a user visiting a page.
	 */
	protected abstract IEventType getPageViewEventType();


	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Event> IModel<T> storeEvent(T event, Component triggeringComponent) {
		gatherEventDataContributions(event, triggeringComponent);
		if (triggeringComponent != null)
			event.setComponentPath(triggeringComponent.getPageRelativePath());
		event.setDefaultValues();
		cwmService.save(event);
		cwmService.flushChanges();
		if (log.isTraceEnabled()) {
			// When tracing, build and output a complete description of the event.
			log.trace("Saved event: {}", ReflectionToStringBuilder.reflectionToString(event), ToStringStyle.MULTI_LINE_STYLE);
		} else {
			log.debug("Saved event: {}", event);
		}
		return modelProvider.modelOf(event);
	}

	/**
	 *  Recursively collect information in the Event from all ancestors of the given Component.
	 *  The top-level Component (page) will contribute its data first, moving down the chain
	 *  to the triggering component last, so that more specific components can override values
	 *  set by more general ones.
	 *  Unchecked cast below - we assume that the Event will be of the correct subtype for each contributor.
	 *  Not sure how that could be avoided.
	 * @param event the event to be modified
	 * @param component the component whose ancestors are consulted
	 */
	@SuppressWarnings("unchecked")
	protected void gatherEventDataContributions (Event event, Component component) {
		if (component == null) {
			log.warn("Event logged with no triggering component: {}", event);
			return;
		}
		if (component.getParent() != null)
			gatherEventDataContributions(event, component.getParent());
		if (component instanceof IEventDataContributor) {
			((IEventDataContributor) component).contributeEventData(event);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public IModel<? extends Event> storeEvent(Component triggerComponent, IEventType type, String detail) {
		Event event = newEvent()
				.setType(type)
				.setDetail(detail);
		return storeEvent(event, triggerComponent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IModel<? extends Event> storeLoginEvent(Component triggerComponent) {
		return storeEvent(triggerComponent, getLoginEventType(),
				"role=" + cwmSessionService.getUser().getRole());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IModel<? extends Event> storePageViewEvent(String pageName, Page page) {
		Event event = newEvent()
				.setType(getPageViewEventType())
				.setPage(pageName);
		return storeEvent(event, page);
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
			loginSession.setCookiesEnabled(info.isNavigatorCookieEnabled());
			loginSession.setPlatform(info.getNavigatorPlatform());
			loginSession.setUserAgent(cwmSession.getClientInfo().getUserAgent());
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
	public IModel<? extends Event> recordLogout(Component triggerComponent) {
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
		// storeEvent will commit the transaction
		return storeEvent(triggerComponent, getLogoutEventType(), sesLength);
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
		ev.setType(getTimeoutEventType());
		ev.setDetail("Session length=" + (now.getTime()-loginSession.getStartTime().getTime())/1000 + "s " + comment);
		ev.setStartTime(now);
		ev.setLoginSession(loginSession);
		ev.setUser(loginSession.getUser());
		storeEvent(ev, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IEventService#getLastEventTime(org.cast.cwm.data.LoginSession)
	 */
	@Override
	public Date getLastEventTime (LoginSession ls) {
		Session session = Databinder.getHibernateSession();
		Criteria criteria = session.createCriteria(Event.class);
		criteria.add(Restrictions.eq("loginSession", ls));
		criteria.setProjection(Projections.max("startTime"));
		return (Date) criteria.uniqueResult();
	}
	
}
