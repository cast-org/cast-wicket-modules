/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2008  Nathan Hamblen nathan@technically.us
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.databinder.hib;


import org.hibernate.SessionFactory;

/**
 * Databinder application interface. DataStaticService expects the current Wicket
 * application to conform to this interface and supply a session factory as needed.
 * @see Databinder
 * @author Nathan Hamblen
 */
public interface HibernateApplication {
	/**
	 * Supply the session factory for the given key. Applications needing only one
	 * session factory may return it without inspecting the key parameter.
	 * @param key or null for the default factory
	 * @return configured Hibernate session factory
	 */
	SessionFactory getHibernateSessionFactory(Object key);
}
