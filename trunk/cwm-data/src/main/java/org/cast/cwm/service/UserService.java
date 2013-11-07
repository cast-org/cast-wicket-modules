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
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
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

import com.google.inject.Inject;

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
public class UserService implements IUserService {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	@Inject
	private ICwmService cwmService;

	@Getter @Setter
	private Class<? extends User> userClass = User.class;

	public UserService() {
		Injector.get().inject(this);
	}
	
	protected static UserService instance = new UserService();
	
	public static UserService get() {
		return instance;
	}

	public static void setInstance(UserService instance) {
		UserService.instance = instance;
	}
	
	/** 
	 * Instantiate an instance of the application's User class.
	 * 
	 * @return
	 */
	@Override
	public User newUser() {
		try {
			return userClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#confirmUser(org.cast.cwm.data.User)
	 */
	@Override
	public void confirmUser(User user) {
		user.setValid(true);
		user.setSecurityToken(null);
		cwmService.flushChanges();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#generateSecurityToken(org.apache.wicket.model.IModel)
	 */
	@Override
	public void generateSecurityToken(IModel<User> mUser) {
		mUser.getObject().generateSecurityToken();
		cwmService.flushChanges();
	}

	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getAllUsers()
	 */
	@Override
	public IModel<List<User>> getAllUsers() {
		return new UserListModel();
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getById(long)
	 */
	@Override
	public IModel<User> getById(long userId) {
		return new UserModel(userId);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getByUsername(java.lang.String)
	 */
	@Override
	public IModel<User> getByUsername (String username) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setUsername(username);
		return new UserModel(c);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getBySubjectId(java.lang.String)
	 */
	@Override
	public IModel<User> getBySubjectId (String subjectId) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setSubjectId(subjectId);
		return new UserModel(c);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getByEmail(java.lang.String)
	 */
	@Override
	public IModel<User> getByEmail (String email) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setEmail(email);
		return new UserModel(c);
	}
	
	// gets both valid and invalid users
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getAllByEmail(java.lang.String)
	 */
	@Override
	public IModel<User> getAllByEmail (String email) {

		// Sort valid users to the top
		SingleSortState sort = new SingleSortState();
		SortParam sortParam = new SortParam("valid", false);
		sort.setSort(sortParam);

		// it is possible to return both valid and invalid
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setEmail(email);
		c.setGetAllUsers(true);
		c.setSortState(sort);
		UserListModel userListModel = new UserListModel(c);
		UserModel mUser = new UserModel(userListModel.getObject().get(0));
		
		return mUser;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getByFullnameFromPeriod(java.lang.String, java.lang.String, org.apache.wicket.model.IModel)
	 */
	@Override
	public IModel<User> getByFullnameFromPeriod(String firstName, String lastName, IModel<Period> period) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setFirstName(firstName);
		c.setLastName(lastName);
		c.setPeriod(period);
		return new UserModel(c);
	}


	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getByRole(org.cast.cwm.data.Role)
	 */
	@Override
	public UserListModel getByRole(Role role) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setRole(role);
		return new UserListModel(c);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getUserListProvider()
	 */
	@Override
	public ISortableDataProvider<User> getUserListProvider() {
		return getUserListProvider(null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getUserListProvider(org.apache.wicket.model.IModel)
	 */
	@Override
	public ISortableDataProvider<User> getUserListProvider(IModel<Period> mPeriod) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setPeriod(mPeriod);
		return new SortableHibernateProvider<User>(User.class, c);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getUncachedUserListProvider(org.apache.wicket.model.IModel)
	 */
	@Override
	public ISortableDataProvider<User> getUncachedUserListProvider(IModel<Period> mPeriod) {
		UserCriteriaBuilder c = new UserCriteriaBuilder();
		c.setPeriod(mPeriod);
		c.setCacheResults(false);
		return new SortableHibernateProvider<User>(User.class, c);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IUserService#getLoginSessions(org.apache.wicket.model.IModel)
	 */
	@Override
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

				@Override
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
