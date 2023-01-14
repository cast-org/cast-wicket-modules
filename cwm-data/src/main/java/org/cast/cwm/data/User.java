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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.databinder.auth.data.DataUser;
import net.databinder.auth.data.hib.BasicPassword;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.db.data.PersistedObject;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * A person who uses the application.  Almost all persistent data in the application
 * is, in some way, linked to a User account.  Usernames must be unique across the
 * application.
 *
 * @author jbrookover
 *
 */
@Entity
@Audited
@Table(name="Users") // Necessary since "user" is a reserved word in SQL
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Getter
@Setter
public class User extends PersistedObject implements Serializable, DataUser, Comparable<User> {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE)
	private Long id;

	@Column(unique=true)
	protected String subjectId;

	/**
	 * Identifier for LTI reference.
	 */
	@Column(unique=false)
	protected String ltiId;

	@Column(nullable=false, columnDefinition="boolean default false")
	protected boolean permission = false;

	@Column(unique=true, nullable=false)
	@Index(name="person_username_idx")
	//@NaturalId(mutable=true)  TODO:Investigate
	protected String username;

	@Column(nullable=false)
	private BasicPassword password;

	@Column(nullable=false)
	@Index(name="people_role_idx")
	@Enumerated(EnumType.STRING)
	protected Role role;

	@ManyToMany
	@JoinTable(
			name="perioduser",
			joinColumns={@JoinColumn(name="user_id")},
			inverseJoinColumns={@JoinColumn(name="period_id")})
	@SortNatural
	protected SortedSet<Period> periods = new TreeSet<Period>();

	/**
	 * A "valid" user can log in and generally use the features of the application.
	 * You may set valid to false if the user should be hidden in lists, prevented from logging in, etc.
	 *
	 * This is false by default.  EditUserPanel, which should be used to create users has a
	 * setAutoConfirm method that will allow you to automatically create valid users.
	 */
	@Column(nullable=false)
	protected boolean valid = false;

	protected String firstName;

	protected String lastName;

	@Column(unique=true)
	protected String email;

	/**
	 * Record of when this user account was created.
	 */
	protected Date createDate;

	/**
	 * Used to store a token (generally a long random string) that can be used in lieu of or in addition to a password.
	 * This is for operations like registering via email or recovering a lost password.
	 */
	protected String securityToken;

	/**
	 * If set, securityToken is only valid until this Date.
	 */
	protected Date securityTokenExpires;

	public User() { /* No Arg Constructor for DataStore */ }

	public User(Role role) {
		this.role = role;
	}

	public boolean hasRole(Role r)  {
		return role.subsumes(r);
	}

	@Override
	public boolean hasRole(String roleString) {
		return hasRole(Role.forRoleString(roleString));
	}

	public boolean isGuest() {
		return role == Role.GUEST;
	}

	/**
	 * Should we expect this user to have connections to specific Periods?
	 * By default students, teachers, and researchers do, but apps may override this.
	 * @return
	 */
	public boolean usesPeriods() {
		return (role!=Role.ADMIN && role!=Role.GUEST);
	}

	public String getFullName() {
		if (!Strings.isEmpty(firstName) && !Strings.isEmpty(lastName)) {
			return firstName + " " + lastName;
		} else if (!Strings.isEmpty(lastName)) {
			return lastName;
		} else if (!Strings.isEmpty(firstName)) {
			return firstName;
		} else if (!Strings.isEmpty(email)){
			return email;
		} else {
			return username;
		}
	}

	public String getSortName() {
		if (!Strings.isEmpty(firstName) && !Strings.isEmpty(lastName)) {
			return lastName + ", " + firstName;
		} else if (!Strings.isEmpty(lastName)) {
			return lastName;
		} else if (!Strings.isEmpty(firstName)) {
			return firstName;
		} else if (!Strings.isEmpty(email)){
			return email;
		} else {
			return username;
		}
	}

	public List<Period> getPeriodsAsList() {
		return new ArrayList<Period>(getPeriods());
	}

	/**
	 * Returns whichever Period is considered the main or default one for this User.
	 *
	 * By default this is just the alphabetically first in their set of periods, but applications
	 * can override this behavior.
	 *
	 * @return a Period, or null if User has no Periods.
	 */
	public Period getDefaultPeriod() {
		SortedSet<Period> periods = getPeriods();
		if (!periods.isEmpty())
			return periods.first();
		else
			return null;
	}

	@Override
	public String toString() {
		return getFullName();
	}

	@Override
	public int compareTo(User o) {
		if (o == null)
			return 1;
		return getSortName().compareTo(o.getSortName());
	}

	/**
	 * Determine whether this User is in the same period as another User.
	 * @param other another User object to compare against
	 * @return true if they have at least one Period in common
	 */
	public boolean hasPeriodInCommonWith(User other) {
		SortedSet<Period> myPeriods = this.getPeriods();
		Hibernate.initialize(myPeriods);
		SortedSet<Period> otherPeriods = other.getPeriods();
		Hibernate.initialize(otherPeriods);

		for (Period p : myPeriods) {
			if (otherPeriods.contains(p))
				return true;
		}
		return false;
	}

	/** Set password to a string value.  The string will be converted to a hashed value before saving.
	 *
	 * @param password cleartext password
	 */
	public void setPassword(String password) {
		this.password = new BasicPassword(password);
	}

	/**
	 * Fill in the securityToken field with a long random string.
	 * If a duration is given, will fill in the expiry time that interval from now.
	 * @param validDuration amount of time, starting now, that the token should be considered valid.
	 */
	public void generateSecurityToken(Duration validDuration) {
		securityToken = RandomStringUtils.random(20, true, true);
		if (validDuration != null)
			securityTokenExpires = Date.from(ZonedDateTime.now().plus(validDuration).toInstant());
	}

	/**
	 * Remove (invalidate) any existing security token.
	 */
	public void removeSecurityToken() {
		securityToken = null;
		securityTokenExpires = null;
	}

	/**
	 * Check whether a given string is a valid security token for the user.
	 * @param token string to be checked against the user's token
	 * @return true if the user has a token, it hasn't expired, and it matches the string
	 */
	public boolean checkSecurityToken(String token) {
		boolean expired = securityTokenExpires != null && securityTokenExpires.before(new Date());
		return (!expired
			&& !Strings.isEmpty(token)
			&& !Strings.isEmpty(securityToken)
			&& token.equals(securityToken));
	}

}
