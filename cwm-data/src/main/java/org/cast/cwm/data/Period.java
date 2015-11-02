/*
 * Copyright 2011-2014 CAST, Inc.
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A named group of {@link User} objects, part of a {@link Site}.
 * Normally, this is used to organize users into classrooms of students and teachers.
 *
 * Users can belong to many Periods.  Any given Period can only
 * belong to one {@link Site}.
 *
 * @author jbrookover
 */
@Entity
@Audited
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Getter
@Setter
@ToString(of={"id", "name", "site"})
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

	/**
	 * Anonymous identifier for research purposes.
	 */
	@Column(unique = true, nullable = false)
	private String classId;
	
	@ManyToMany(mappedBy="periods")
	@SortNatural
	private SortedSet<User> users = new TreeSet<User>();
	
	public Period() { /* No Arg Constructor for the datastore */ }
	
	/**
	 * Get a set of users in this period, filtered by
	 * a specific role.
	 *
	 * @deprecated {@link org.cast.cwm.data.builders.UserCriteriaBuilder is more efficient and flexible}
	 *
	 * @param role return only users with at least this level of permission
	 * @return a set of users, sorted in natural order
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
	 * Periods are sorted alphabetically by name within a site.
	 * This function is careful to avoid NPEs and avoids returning 0 for two 
	 * Periods that have different IDs.
	 * @param other Period to compare to
	 */
	@Override
	public int compareTo(Period other) {
		if (this.equals(other))
			return 0;

		// .getSite() not .site so that we correctly work through Hibernate proxies
		int siteDiff = ObjectUtils.compare(this.getSite(), other.getSite());
		if (siteDiff != 0)
			return siteDiff;

		int nameDiff = ObjectUtils.compare(this.getName(), other.getName());
		if (nameDiff != 0)
			return nameDiff;

		return ObjectUtils.compare(this.getId(), other.getId());
	}

}
