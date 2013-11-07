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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.databinder.auth.data.DataUser;
import net.databinder.auth.data.hib.BasicPassword;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.wicket.util.string.Strings;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.envers.Audited;

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
	@Sort(type=SortType.NATURAL)
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
	
	/** Set password to a string value.  The string will be converted to a hashed value before saving.
	 * 
	 * @param password cleartext password
	 */
	public void setPassword(String password) {
		this.password = new BasicPassword(password);
	}
	
	/**
	 * Fill in the securityToken field with a long random string.
	 */
	public void generateSecurityToken() {
		securityToken = RandomStringUtils.random(12, true, true);
	}

}
