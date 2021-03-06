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
package net.databinder;

import java.util.HashSet;

import net.databinder.hib.Databinder;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Opens Hibernate sessions and transactions as required and closes them at a request's
 * end. Uncomitted transactions are rolled back. Uses keyed Hibernate session factories from
 * Databinder service.</p>
 * @see Databinder
 * @author Nathan Hamblen
 */
public class DBRequestCycleListener implements IRequestCycleListener {

	/** Keys for session factories that have been opened for this request */ 
	protected HashSet<Object> keys = new HashSet<Object>();

	private static final Logger log = LoggerFactory.getLogger(DBRequestCycleListener.class);

	public DBRequestCycleListener() {
	}

	/** Roll back active transactions and close session. */
	protected void closeSession(Object key) {
		Session sess = Databinder.getHibernateSession(key);
		
		if (sess.isOpen())
			try {
				if (sess.getTransaction().isActive()) {
					log.debug("Rolling back uncomitted transaction.");
					sess.getTransaction().rollback();
				}
			} finally {
				sess.close();
			}
	}

	/**
	 * Called by DataStaticService when a session is needed and does not already exist. 
	 * Opens a new thread-bound Hibernate session.
	 */
	public void dataSessionRequested(Object key) {
		openHibernateSession(key);
	}
	
	/**
	 * Open a session and begin a transaction for the keyed session factory.
	 * @param key object, or null for the default factory
	 * @return newly opened session
	 */
	protected org.hibernate.Session openHibernateSession(Object key) {
		org.hibernate.Session sess = Databinder.getHibernateSessionFactory(key).openSession();
		sess.beginTransaction();
		ManagedSessionContext.bind(sess);
		keys.add(key);
		return sess;
	}

	@Override
	public void onBeginRequest(RequestCycle cycle) {
		log.debug("onBeginRequest: {}", cycle.getRequest().getUrl());
	}

	/**
	 * Closes all Hibernate sessions opened for this request. If a transaction has
	 * not been committed, it will be rolled back before closing the session.
	 * @see net.databinder.components.hib.DataForm#onSubmit()
	 */
	@Override
	public void onEndRequest(RequestCycle cycle) {
		for (Object key : keys) {
			SessionFactory sf = Databinder.getHibernateSessionFactory(key);
			if (ManagedSessionContext.hasBind(sf)) {
				closeSession(key);
				ManagedSessionContext.unbind(sf);
			}
		}
		log.debug("onEndRequest complete {}", cycle.getRequest().getUrl());
	}

	@Override
	public void onDetach(RequestCycle cycle) {
		// no action needed
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
		// no action needed
	}

	@Override
	public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
		// no action needed
	}

	/** 
	 * Closes and reopens sessions for this request cycle. Unrelated models may try to load 
	 * themselves after this point. 
	 */
	@Override
	public IRequestHandler onException(RequestCycle cycle, Exception ex) {
		onEndRequest(cycle);
		onBeginRequest(cycle);
		return null;
	}

	@Override
	public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
		// no action needed
	}

	@Override
	public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
		// no action needed
	}

	@Override
	public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
		// no action needed
	}

}
