package org.cast.cwm.service;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;


public interface IUserPreferenceService {

	/**
	 * Set the user preference.  Create it if it doesn't exist.
	 * @param mUser
	 * @param name
	 * @param booleanValue
	 */
	void setUserPreferenceBoolean(IModel<User> mUser, String name, Boolean booleanValue);

	/**
	 * Get the user preference by identifier.  Null if there is no preference
	 * 
	 * @param mUser
	 * @param identifier
	 * @return booleanValue
	 */
	Boolean getUserPreferenceBoolean(IModel<User> mUser, String name);

}
