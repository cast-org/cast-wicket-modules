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