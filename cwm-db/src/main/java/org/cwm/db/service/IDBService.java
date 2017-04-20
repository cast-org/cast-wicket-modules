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
package org.cwm.db.service;

import java.io.Serializable;

import com.google.inject.ImplementedBy;
import net.databinder.hib.SessionUnit;

import org.hibernate.Session;

/**
 * An injectable service for interacting with Hibernate and cwm-db classes.
 * 
 * @author bgoldowsky
 *
 */
@ImplementedBy(DBService.class)
public interface IDBService {

	/**
	 * Get the Hibernate session attached to the current thread.
	 * @return the Hibernate session
	 */
	public Session getHibernateSession();

	/**
	 * Run the given SessionUnit in a thread with a Hibernate session.
	 * See {@link net.databinder.hib.Databinder#ensureSession}
	 * @param unit SessionUnit to be run
	 * @return value returned by the SessionUnit
	 */
	public Object ensureHibernateSession(SessionUnit unit);

	/**
	 * Persist the given object into the database.
	 *
	 * @param persistableObject an object mapped to a database class
	 * @return the saved object.  An identifier will have been generated.
	 */
	public Serializable save(Object persistableObject);

	/**
	 * Take an object that might be a Hibernate proxy, initialize lazy values and unwrap it from the proxy.
	 * Useful since proxies are not necessarily <code>instanceof</code> the proper class.
	 *
	 * Taken from http://stackoverflow.com/questions/2216547/converting-hibernate-proxy-to-real-object .
	 *
	 * @param entity Hibernate proxy object (or something that might be one)
	 * @param <T> can be of any type.
	 * @return the initialized "real" object represented by the proxy.
	 */
	public <T> T initializeAndUnproxy(T entity);

}
