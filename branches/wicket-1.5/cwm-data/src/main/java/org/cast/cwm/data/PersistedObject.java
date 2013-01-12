/*
 * Copyright 2011-2013 CAST, Inc.
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
package org.cast.cwm.data;

import java.io.Serializable;

import org.hibernate.proxy.HibernateProxy;

/**
 * <p>
 * A base class that all persisted objects extend.  It
 * provides {@link #equals(Object)} and {@link PersistedObject#hashCode()}
 * methods that use the datastore ID for comparison purposes.  
 * </p>
 * <p>
 * It also considers hibernate proxies when comparing class types.
 * 
 * TODO: Shouldn't reference hibernate!
 * </p>
 */
public abstract class PersistedObject implements Serializable {

  private static final long serialVersionUID = 1L;
  
  /**
   * Returns true if this object has not yet been
   * persisted to the datastore.
   * 
   * TODO: Fails if an object is attached to another Persistent
   * object, but then not saved and held over to another request.
   * @return
   */
  public boolean isTransient() {
	  return getId() == null || getId() == 0;
  }

  /**
   * only returns true if the objects are of the same class and the ids are equal
   * @param obj - the object to compare against
   */
  @Override
  public boolean equals(Object obj) {
	  if(this == obj) 
		  return true;
	  if(obj == null) 
		  return false;
	  
	  Class<?> otherClass;
	  Class<?> thisClass;
	  
	  
	  // TODO: Is a reference to Hibernate really necessary?
	  // Can we just be better about using models?
	  if (obj instanceof HibernateProxy)
		  otherClass = ((HibernateProxy) obj).getHibernateLazyInitializer().getPersistentClass();
	  else
		  otherClass = obj.getClass();
	  if (this instanceof HibernateProxy)
		  thisClass = ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass();
	  else
		  thisClass = getClass();
	  if (!thisClass.equals(otherClass))
		  return false;
	  
	  PersistedObject other = (PersistedObject) obj;
	 
	  return getId() != null && getId().equals(other.getId());
  }
  
  /**
   * if the object has a valid id that is returned as the hashcode
   * otherwise uses super.hashCode()
   * TODO:  This is bad, and not evenly distributed over ints.  BAD HASH!
   * 
   */
  @Override
  public int hashCode() {
    if(getId() != null && !getId().equals(new Long(0)))
      return getId().intValue();
    return super.hashCode();
  }
  
  
  @Override
  public String toString() {
	  return this.getClass().toString() + "[" + this.getId() + "]";
  }
  
  public abstract Long getId();

}