package net.databinder;

import java.util.HashSet;

import net.databinder.hib.Databinder;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.ManagedSessionContext;
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
	protected org.hibernate.classic.Session openHibernateSession(Object key) {
		org.hibernate.classic.Session sess = Databinder.getHibernateSessionFactory(key).openSession();
		sess.beginTransaction();
		ManagedSessionContext.bind(sess);
		keys.add(key);
		return sess;
	}

	public void onBeginRequest(RequestCycle cycle) {
		// no action needed
	}

	/**
	 * Closes all Hibernate sessions opened for this request. If a transaction has
	 * not been committed, it will be rolled back before closing the session.
	 * @see net.databinder.components.hib.DataForm#onSubmit()
	 */
	public void onEndRequest(RequestCycle cycle) {
		for (Object key : keys) {
			SessionFactory sf = Databinder.getHibernateSessionFactory(key);
			if (ManagedSessionContext.hasBind(sf)) {
				closeSession(key);
				ManagedSessionContext.unbind(sf);
			}
		}
	}

	public void onDetach(RequestCycle cycle) {
		// no action needed
	}

	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
		// no action needed
	}

	public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
		// no action needed
	}

	/** 
	 * Closes and reopens sessions for this request cycle. Unrelated models may try to load 
	 * themselves after this point. 
	 */
	public IRequestHandler onException(RequestCycle cycle, Exception ex) {
		onEndRequest(cycle);
		onBeginRequest(cycle);
		return null;
	}

	public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
		// no action needed
	}

	public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
		// no action needed
	}

	public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
		// no action needed
	}

}
