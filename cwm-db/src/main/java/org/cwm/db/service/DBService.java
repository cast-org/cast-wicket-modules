package org.cwm.db.service;

import net.databinder.hib.Databinder;
import net.databinder.hib.SessionUnit;

import org.hibernate.Session;

/**
 * An injectable service for interacting with Hibernate and cwm-db classes.
 * 
 * At the moment, this is just a facade in front of a few static methods provided 
 * by the Databinder class.  However, it may eventually become the primary way to 
 * access database functions.
 * 
 * @author bgoldowsky
 *
 */
public class DBService implements IDBService {

	public DBService() {
	}

	public Session getHibernateSession() {
		return Databinder.getHibernateSession();
	}

	public Object ensureHibernateSession(SessionUnit unit) {
		return Databinder.ensureSession(unit);
	}

}
