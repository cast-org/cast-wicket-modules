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
package org.cast.cwm.data;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.envers.Audited;

/**
 * <p>
 * A named group of {@link User} objects.  Conceptually, this should be
 * used to organize users into classrooms of students and teachers.
 * </p>
 * <p>
 * Users can belong to many Periods.  Any given Period can only
 * belong to one {@link Site}.
 * </p>  
 * @author jbrookover
 *
 */
@Entity
@Audited
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Getter
@Setter
@ToString(of={"id", "name"})
public class Period extends PersistedObject implements Comparable<Period> {
  
	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;
	
	@NaturalId(mutable=true)
	@ManyToOne(optional=false)
	private Site site;
	
	@NaturalId(mutable=true)
	private String name;
	
	@ManyToMany(mappedBy="periods")
	@Sort(type=SortType.NATURAL)
	private SortedSet<User> users = new TreeSet<User>();
	
	public Period() { /* No Arg Constructor for the datastore */ }
	
	/**
	 * <p>
	 * Get a set of users in this period, filtered by 
	 * a specific role.
	 * </p>
	 * <p>
	 * Deprecated in favor of using models.
	 * </p>
	 * @param role
	 * @return
	 */
	@Deprecated
	public SortedSet<User> getByRole (Role role) {
		SortedSet<User> filteredUsers = new TreeSet<User>();
		for (User user : getUsers()) {
			if (user.hasRole(role))
				filteredUsers.add(user);
		}
		return filteredUsers;		
	}

	/**
	 * Periods are sorted alphabetically by name.
	 * This function is careful to avoid NPEs and avoids returning 0 for two 
	 * Periods that have different IDs.
	 */
	public int compareTo(Period other) {
		if (other == null)
			return 1;
		int idDiff = (getId()==null) ? 0 : getId().compareTo(other.getId());
		if (name == null || other.name == null)
			return idDiff;
		int nameDiff = name.compareTo(other.name);
		if (nameDiff != 0)
			return nameDiff;
		return idDiff;
	}
}
