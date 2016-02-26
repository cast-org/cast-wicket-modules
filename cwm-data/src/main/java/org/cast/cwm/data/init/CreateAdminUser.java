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
package org.cast.cwm.data.init;

import net.databinder.hib.Databinder;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.IAppConfiguration;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.service.IUserService;
import org.hibernate.Session;

import com.google.inject.Inject;

/**
 * This initializer makes sure that an administrative user is defined.
 * If a adminPassword is set in the application properties, it will reset the admin password to that value even if the
 * user already exists.  This can be used to reset a lost admin password.
 * 
 * Application properties read:
 *   cwm.adminUsername
 *   cwm.adminPassword
 *   cwm.adminEmail
 * 
 * @author bgoldowsky
 *
 */
public class CreateAdminUser implements IDatabaseInitializer {
	
	private static final String DEFAULT_ADMIN_USERNAME = "admin";
	private static final String DEFAULT_ADMIN_PASSWORD = "admin";

	@Inject 
	IUserService userService;
	
	public CreateAdminUser() {
		Injector.get().inject(this);
	}
	
	@Override
	public String getName() {
		return "create admin user";
	}
	
	@Override
	public boolean isOneTimeOnly() {
		// False because this also can be used to reset the admin user's password in case of forgetfulness.
		return false;
	}

	@Override
	public boolean run(IAppConfiguration props) {
		String username = props.getProperty("cwm.adminUsername");
		if (Strings.isEmpty(username))
			username = DEFAULT_ADMIN_USERNAME;
		String email = props.getProperty("cwm.adminEmail");
		String password = props.getProperty("cwm.adminPassword");
		boolean passwordSpecified = !Strings.isEmpty(password);
		if (!passwordSpecified)
			password = DEFAULT_ADMIN_PASSWORD;
		return createAdminUser(username, password, email, passwordSpecified);
	}

	/** 
	 * Create or reset administrative user account.
     * Calling this method ensures that a user with the given username exists; one
     * will be created if necessary.  
     * If the password argument is non-null, then even if the user already exists,
     * the password will be reset to that value.  Expected usage is to take this value 
     * from an application properties file so it can be used to override the app-specified
     * default password, and can be used to reset the admin password if the current
     * password has been forgotten.
     * 
     * If password is null then the administrator's password will not be changed, 
     * but if a new account needs to be created then the default password will be used.
     * @param username for administrative user
     * @param password for administrative user
	 * @param email for administrative user
	 * @param resetPassword should be true to force a password reset
	 * @return true if changes were made to the datastore, false if no changes were made.  Changes must be committed.
     */
	public boolean createAdminUser (final String username, final String password, String email, final boolean resetPassword) {
		Session session = Databinder.getHibernateSession();
		User admin = userService.getByUsername(username).getObject();
		if (admin == null) {
			admin = userService.newUser();
			admin.setRole(Role.ADMIN);
			admin.setFirstName("Administrator");
			admin.setLastName("Account");
			admin.setUsername(username);
			admin.setSubjectId(username);
			admin.setPassword(password);
			admin.setEmail(email);
			admin.setValid(true);
			session.save(admin);
			return true;
		} else if (resetPassword) {
			admin.setPassword(password);
			return true;
		}
		return false;
	}
	
}
