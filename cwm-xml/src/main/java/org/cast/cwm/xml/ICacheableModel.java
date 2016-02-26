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
package org.cast.cwm.xml;

import java.io.Serializable;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.time.Time;

/**
 * A Model that can be used for caching derived objects.
 * It must implement a getKey() method that returns a cache key uniquely identifying the model object,
 * and a getLastModified() method returning the model object's last modification time.
 * 
 * @author borisgoldowsky
 *
 * @param <T>
 */
public interface ICacheableModel<T> extends IModel<T> {

	/**
	 * Return the time of the last update to the Model's object; or null if it won't ever change.
	 * When this changes, cached information based on this object needs to be updated.
	 */
	public abstract Time getLastModified();
	
	/**
	 * Return a hash key for this model's object.  This is similar to hashCode() (and can even fall
	 * back on hashCode()), but must be done explicitly.  This is because some implementations of 
	 * hashCode() (e.g. HibernateObjectModel) rely on the existence of a datastore object.  Changes
	 * to the underlying datastore can change the hashCode or throw a NullPointerException.
	 *
	 */
	public abstract Serializable getKey();

}
