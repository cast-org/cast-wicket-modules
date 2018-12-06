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
package org.cast.cwm.service;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;

/**
 * Version of @UserPreferenceService for GUEST type Users, which doesn't refer to database.
 * All preference values will be reported as null, and setters a no-ops.
 * 
 * @author bgoldowsky
 *
 */
public class GuestUserPreferenceService implements IUserPreferenceService {

	public GuestUserPreferenceService() {
	}
		
	public void setUserPreferenceBoolean(IModel<User> mUser, String name, Boolean booleanValue) {
	}
	
	public Boolean getUserPreferenceBoolean(IModel<User> mUser, String name) {
		return null;
	}
		
	@Override
	public boolean getUserPreferenceBoolean(IModel<User> mUser, String name, boolean defaultValue) {
		return defaultValue;
	}

	public void setUserPreferenceString(IModel<User> mUser, String name, String stringValue) {
	}

	public String getUserPreferenceString(IModel<User> mUser, String name) {
		return null;
	}

	@Override
	public String getUserPreferenceString(IModel<User> mUser, String name, String defaultValue) {
		return defaultValue;
	}

}