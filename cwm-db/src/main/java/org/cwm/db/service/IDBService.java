/*
 * Copyright 2011-2019 CAST, Inc.
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

import net.databinder.hib.SessionUnit;

import org.hibernate.Session;

/**
 * An injectable service for interacting with Hibernate and cwm-db classes.
 * 
 * @author bgoldowsky
 *
 */
public interface IDBService {
	
	public abstract Session getHibernateSession();

	public abstract Object ensureHibernateSession(SessionUnit unit);
	
	public abstract Serializable save(Object persistableObject);

}
