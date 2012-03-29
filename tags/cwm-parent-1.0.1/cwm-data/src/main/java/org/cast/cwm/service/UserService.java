/*
 * Copyright 2011 CAST, Inc.
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
package org.cast.cwm.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.databinder.models.hib.HibernateObjectModel;
import net.databinder.models.hib.QueryBuilder;
import net.databinder.models.hib.SortableHibernateProvider;

import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.cast.cwm.admin.EditUserPanel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.builders.UserCriteriaBuilder;
import org.cast.cwm.data.models.UserListModel;
import org.cast.cwm.data.models.UserModel;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database operations on Users and related classes.
 * This is a singleton Service class. Actions may be overridden with a subclass;
 * use setInstance() to register your alternative version.
 * 
 * Default implementation uses Hibernate.
 *  
 * @author bgoldowsky
 *
 */
public class UserService {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	@Getter @Setter
	private Class<? extends User> userClass = User.class;
	
	protected static UserService instance = new UserService();
	
	public static UserService get() {
		return (UserService)instance;
	}

	public static void setInstance(UserService instance) {
		UserService.instance = instance;
	}
	
	/** 
	 * Instantiate an instance of the application's User class.
	 * 
	 * @return
	 */
	public User newUser() {
		try {
			return userClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Mark a user as valid in the database.  Commits changes
	 * to the database at the end of the call.  Any methods
	 * that override this should call the super method at
	 * the end. 
	 * 
	 * see: {@link EditUserPanel#setAutoConfirmNewUser(boolean)}
	 *
	 * @param user
	 */
	public void confirmUser(User user) {
		user.setValid(true);
		user.setSecurityToken(null);
		CwmService.get().flushChanges();
	}
	
	public void generateSecurityToken(IModel<User> mUser) {
		mUser.getObject().generateSecurityToken();
		CwmService.get().flushChanges();
	}

	
	public IModel<List<User>> getAllUsers() {
		return new UserListModel();
	}

	public IModel<User> getById(long userId) {
		return new UserModel(userId);
	}

	public IModel<User> getByUsername (String username) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setUsername(username);
		return new UserModel(c);
	}
	
	public IModel<User> getBySubjectId (String subjectId) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setSubjectId(subjectId);
		return new UserModel(c);
	}
	
	public IModel<User> getByEmail (String email) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setEmail(email);
		return new UserModel(c);
	}
	
	public IModel<User> getByFullnameFromPeriod(String firstName, String lastName, IModel<Period> period) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setFirstName(firstName);
		c.setLastName(lastName);
		c.setPeriod(period);
		return new UserModel(c);
	}


	public UserListModel getByRole(Role role) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setRole(role);
		return new UserListModel(c);
	}
	
	public ISortableDataProvider<User> getUserListProvider() {
		return getUserListProvider(null);
	}
	
	public ISortableDataProvider<User> getUserListProvider(IModel<Period> mPeriod) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setPeriod(mPeriod);
		return new SortableHibernateProvider<User>(User.class, c);
	}
	
	/**
	 * Get an object that will give the total number of logins
	 * and the latest login date for a particular user.  This is a
	 * (somewhat) efficient query.
	 * 
	 * @param user
	 * @return
	 */
	public LoginData getLoginSessions(IModel<User> user) {
		return new LoginData(user);
	}
	
	public static class LoginData implements Serializable  {

		private static final long serialVersionUID = 1L;
		private IModel<User> userModel;
		private HibernateObjectModel<Object[]> queryModel;
		
		public LoginData(IModel<User> user) {
			userModel = user;
			queryModel = new HibernateObjectModel<Object[]>(new QueryBuilder() {

				private static final long serialVersionUID = 1L;

				public Query build(Session hibernateSession) {
					Query q = hibernateSession.createQuery("select count(S), max(S.startTime) from LoginSession S where S.user = :user");
					q.setParameter("user", userModel.getObject());
					q.setCacheable(true);
					return q;	
				}
			});
		}
		
		public Long getLoginCount() {
			return queryModel.getObject() == null ? 0 : (Long) queryModel.getObject()[0];
		}
		
		public Date getLastLogin() {
			return queryModel.getObject() == null ? null : (Date) queryModel.getObject()[1];
		}
	}

}
