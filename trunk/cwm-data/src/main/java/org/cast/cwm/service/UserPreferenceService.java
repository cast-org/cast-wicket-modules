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

import net.databinder.hib.Databinder;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.data.UserPreference;
import org.cast.cwm.data.UserPreferenceBoolean;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Database operations on UsersPreferences.
 * This is a singleton Service class. Actions may be overridden with a subclass;
 * use setInstance() to register your alternative version.
 * 
 * Default implementation uses Hibernate.
 * 
 * @author lynnmccormack
 *
 */
public class UserPreferenceService implements IUserPreferenceService {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	@Inject
	private ICwmService cwmService;

	protected UserPreferenceService() { /* Protected Constructor - use injection */
	}
	
	public static void setInstance(UserService instance) {
		UserService.instance = instance;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.isi.service.IUserService#setUserPreference(org.apache.wicket.model.IModel, java.lang.String, org.cast.isi.data.PreferenceType)
	 */
	public void setUserPreferenceBoolean(IModel<User> mUser, String name, Boolean booleanValue) {
		
		// if a user preference doesn't exist, create it, otherwise update
		UserPreferenceBoolean userPreference = (UserPreferenceBoolean) getUserPreference(UserPreferenceBoolean.class, mUser, name).uniqueResult();
		if (userPreference == null) {
			userPreference = new UserPreferenceBoolean();
			userPreference.setUser(mUser.getObject());
			userPreference.setName(name);
			userPreference.setBooleanValue(booleanValue);
			Databinder.getHibernateSession().save(userPreference);
		} else {			
			userPreference.setBooleanValue(booleanValue);
		}
		cwmService.flushChanges();
	}
	

	/* (non-Javadoc)
	 * @see org.cast.isi.service.IUserService#setUserPreference(org.apache.wicket.model.IModel, java.lang.String)
	 */
	public Boolean getUserPreferenceBoolean(IModel<User> mUser, String name) {
		UserPreferenceBoolean userPreference = (UserPreferenceBoolean) getUserPreference(UserPreferenceBoolean.class, mUser, name).uniqueResult();
		if (userPreference == null) {
			return null;
		}
		return userPreference.getBooleanValue();
	}
	
	
	/**
	 * Retrieve the user preference from DB
	 * @param userPreferenceClass
	 * @param mUser
	 * @param name
	 * @return a generic criteria that needs to be cast to a specific user preference type
	 */
	protected Criteria getUserPreference (Class<? extends UserPreference> userPreferenceClass, IModel<User> mUser, String name) {
		Criteria c = Databinder.getHibernateSession().createCriteria(userPreferenceClass)
				.add(Restrictions.eq("user", mUser.getObject()))
				.add(Restrictions.eq("name", name))
				.setCacheable(true);		
		return c;		
	}
	
}