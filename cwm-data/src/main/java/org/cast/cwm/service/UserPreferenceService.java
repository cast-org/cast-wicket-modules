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
import org.cast.cwm.data.UserPreferenceString;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Inject;

/**
 * Database operations on UsersPreferences.
 * Default implementation uses Hibernate to save and load preferences.
 * TODO: consider caching some values in the session or memory-based cache to avoid extra queries.
 * 
 * @author lynnmccormack
 *
 */
public class UserPreferenceService implements IUserPreferenceService {

	@Inject
	private ICwmService cwmService;
	
	public UserPreferenceService() {
	}
		
	public void setUserPreferenceBoolean(IModel<User> mUser, String name, Boolean booleanValue) {
					
		// if a user preference doesn't exist, create it, otherwise update
		UserPreferenceBoolean userPreference = (UserPreferenceBoolean) getUserPreferenceCriteria(UserPreferenceBoolean.class, mUser, name).uniqueResult();
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
	
	public Boolean getUserPreferenceBoolean(IModel<User> mUser, String name) {
		UserPreferenceBoolean userPreference = (UserPreferenceBoolean) getUserPreferenceCriteria(UserPreferenceBoolean.class, mUser, name).uniqueResult();
		if (userPreference == null) {
			return null;
		}
		return userPreference.getBooleanValue();
	}
		
	public void setUserPreferenceString(IModel<User> mUser, String name, String stringValue) {
		// if a user preference doesn't exist, create it, otherwise update
		UserPreferenceString userPreference = (UserPreferenceString) getUserPreferenceCriteria(UserPreferenceString.class, mUser, name).uniqueResult();
		Session session = Databinder.getHibernateSession();
		if (userPreference == null) {
			userPreference = new UserPreferenceString();
			userPreference.setUser(mUser.getObject());
			userPreference.setName(name);
			userPreference.setStringValue(stringValue);
			session.save(userPreference);
		} else {			
			userPreference.setStringValue(stringValue);
			session.update(userPreference);
		}

		cwmService.flushChanges();
	}

	public String getUserPreferenceString(IModel<User> mUser, String name) {
		Criteria criteria = getUserPreferenceCriteria(UserPreferenceString.class, mUser, name);
		UserPreferenceString preference = (UserPreferenceString) criteria.uniqueResult();
		return (preference==null) ? null : preference.getStringValue();
	}
	
	/**
	 * Retrieve the user preference from DB
	 * @param userPreferenceClass
	 * @param mUser
	 * @param name
	 * @return a criteria query to look up the indicated preference
	 */
	protected Criteria getUserPreferenceCriteria (Class<? extends UserPreference> userPreferenceClass, IModel<User> mUser, String name) {
		Criteria c = Databinder.getHibernateSession().createCriteria(userPreferenceClass)
				.add(Restrictions.eq("user", mUser.getObject()))
				.add(Restrictions.eq("name", name))
				.setCacheable(true);		
		return c;		
	}

}