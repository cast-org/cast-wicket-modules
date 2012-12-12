package org.cwm.db.service;

import net.databinder.hib.SessionUnit;

import org.hibernate.Session;

/**
 * An injectable service for interacting with Hibernate and cwm-db classes.
 * 
 * @author bgoldowsky
 *
 */
public interface IDBService {
	
	public Session getHibernateSession();

	public Object ensureHibernateSession(SessionUnit unit);

}
