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
package org.cast.cwm.db.data;

import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;

/**
 * A base class that persisted objects can extend.  
 * It provides reasonable implementations of 
 * {@link #equals(Object)} and {@link #hashCode()} for this type of object,
 * using the primary key ID for comparison purposes.
 *
 * It also considers hibernate proxies when comparing class types.
 * 
 */
public abstract class PersistedObject implements Serializable {

    private static final long serialVersionUID = 1L;
  
	/**
	 * Return the primary key ID that will be used for equality comparisons.
	 * @return the id
	 */
	public abstract Long getId();

  /**
   * Returns true if this object has not yet been
   * persisted to the datastore.
   * 
   * TODO: Fails if an object is attached to another Persistent
   * object, but then not saved and held over to another 
   * @return true if object is transient (not yet persisted)
   */
  public boolean isTransient() {
	  return getId() == null || getId() == 0;
  }

  /**
   * Equality comparison based on only returns true if the objects are of the same class and the ids are equal.
   * The class comparison handles the case where one or both of the objects may be Hibernate proxies.
   * Note that any two distinct transient objects will not be considered equal.
   * 
   * @param other - the object to compare against
   * @return true if equal
   */
  @Override
  public boolean equals(Object other) {
	  if(this == other)
		  return true;
	  if(other == null)
		  return false;

	  if (!(other instanceof PersistedObject))
		return false;

	  PersistedObject thisInit = this;
	  if (this instanceof HibernateProxy) {
		  thisInit = (PersistedObject) ((HibernateProxy) this).getHibernateLazyInitializer()
				  .getImplementation();
	  }

	  PersistedObject otherInit = (PersistedObject) other;
	  if (other instanceof HibernateProxy) {
		  otherInit = (PersistedObject) ((HibernateProxy) otherInit).getHibernateLazyInitializer()
				  .getImplementation();
	  }

	  if (!thisInit.getClass().equals(otherInit.getClass()))
		  return false;
	  
	  if (thisInit.isTransient() || otherInit.isTransient())
		  return false;

	  return thisInit.getId().equals(otherInit.getId());
  }
  
  /**
   * Hash is based on the ID of the object if it has one.
   * Otherwise, since transient objects will not compare as equal,
   * we can use the super implementation, {@link Object#hashCode()}.
   */
  @Override
  public int hashCode() {
		Long id = getId();
		if (id != null && id.longValue() != 0L)
			return id.hashCode();
		else
			return super.hashCode();
  }
    
  @Override
  public String toString() {
	  return this.getClass().toString() + "[" + this.getId() + "]";
  }

}
