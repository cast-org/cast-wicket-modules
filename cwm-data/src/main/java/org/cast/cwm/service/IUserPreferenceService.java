/*
 * Copyright 2011-2016 CAST, Inc.
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

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;


public interface IUserPreferenceService {

	/**
	 * Set the boolean-valued user preference.  Create it if it doesn't exist.
	 * @param mUser
	 * @param name
	 * @param booleanValue
	 */
	public void setUserPreferenceBoolean(IModel<User> mUser, String name, Boolean booleanValue);

	/**
	 * Get the boolean-valued user preference by identifier.  Null if there is no preference
	 * 
	 * @param mUser
	 * @param identifier
	 * @return booleanValue
	 */
	public Boolean getUserPreferenceBoolean(IModel<User> mUser, String name);
	
	/**
	 * Get the boolean-valued user preference, with a specified default value.
	 * If no preference exists in the database, the given default will be returned.
	 * 
	 * @param mUser
	 * @param identifier
	 * @param defaultValue
	 * @return boolean value
	 */
	public boolean getUserPreferenceBoolean(IModel<User> mUser, String name, boolean defaultValue);

	/**
	 * Set the string-valued user preference.  Create it if it doesn't exist.
	 * @param mUser
	 * @param name
	 * @param stringValue
	 */
	public void setUserPreferenceString(IModel<User> mUser, String name, String stringValue);

	/**
	 * Get the string-valued user preference by identifier.  Null if there is no preference
	 * 
	 * @param mUser
	 * @param identifier
	 * @return string value
	 */
	public String getUserPreferenceString(IModel<User> mUser, String name);
	
	/**
	 * Get the string-valued user preference, with the specified default value.
	 * If no preference exists in the database, the given default will be returned.
	 * 
	 * @param mUser
	 * @param identifier
	 * @param defaultValue
	 * @return string value
	 */
	public String getUserPreferenceString(IModel<User> mUser, String name, String defaultValue);


}
