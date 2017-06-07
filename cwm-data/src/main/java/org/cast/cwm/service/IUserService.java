/*
 * Copyright 2011-2017 CAST, Inc.
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

import com.google.inject.ImplementedBy;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.cast.cwm.admin.EditUserPanel;
import org.cast.cwm.data.LoginSession;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.UserListModel;
import org.cast.cwm.service.UserService.LoginData;

import java.util.List;

@ImplementedBy(UserService.class)
public interface IUserService {
	
	/**
	 * Return the proper type of User objects for this application.
	 * Normally @User, but subclassses may override it with a customized subclass.
	 * @return class of User
	 */
	Class<? extends User> getUserClass();

	/**
	 * Create a new transient User object of the correct class.
	 * (Applications may override this in a subclass to use a customized User class).
	 */
	User newUser();

	/**
	 * Mark a user as valid in the database.  Commits changes
	 * to the database at the end of the call.  Any methods
	 * that override this should call the super method at
	 * the end. 
	 * 
	 * see: {@link EditUserPanel#setAutoConfirmNewUser(boolean)}
	 *
	 * @param user User to mark as confirmed.
	 */
	void confirmUser(User user);

	/**
	 * Override to take action after a User is created by some administrative action.
	 *
	 * @param mUser Model of the User that was just added.
	 * @param trigger component that triggered the add.
	 */
	void onUserCreated(IModel<User> mUser, Component trigger);

	/**
	 * Override to take action after a User is updated by some administrative action.
	 * Does nothing by default.
	 * @param mUser Model of the User that was changed.
	 * @param trigger component that triggered the update
	 */
	void onUserUpdated(IModel<User> mUser, Component trigger);

	void generateSecurityToken(IModel<User> mUser);

	IModel<List<User>> getAllUsers();

	IModel<User> getById(long userId);

	IModel<User> getByUsername(String username);

	IModel<User> getBySubjectId(String subjectId);

	IModel<User> getByEmail(String email);

	// gets both valid and invalid users
	IModel<User> getAllByEmail(String email);

	IModel<User> getByFullnameFromPeriod(String firstName,
										 String lastName, IModel<? extends Period> period);

	/**
	 * Find all defined users with a specific role, eg all researchers.
	 * @param role the role to find
	 * @return model of the list of users
	 */
	UserListModel getByRole(Role role);

	/**
	 * Find users with a specific role, eg all teachers, in a given Period.
	 * @param mPeriod model of Period
	 * @param role the Role to find
	 * @return model of the list of users
	 */
	UserListModel getByRole(IModel<Period> mPeriod, Role role);

	ISortableDataProvider<User,String> getUserListProvider();

	ISortableDataProvider<User,String> getUserListProvider(
			IModel<? extends Period> mPeriod);

	/**
	 * Find Users with a given Role in a given Period.
	 * For instance, this can find all the students in a classroom.
	 *
	 * @param mPeriod model of period to query
	 * @param role Role of users to return
	 * @return a sortable DataProvider with the list.
	 */
	ISortableDataProvider<User,String> getUserListProvider(IModel<? extends Period> mPeriod, Role role);

	ISortableDataProvider<User,String> getUncachedStudentListProvider(IModel<? extends Period> mPeriod);

	/**
	 * Return any open LoginSession for this user.
	 * (That is, where the end time is null).
	 *
	 * @param mUser model of the user to look for
	 * @return Model of a List of LoginSessions.
	 */
	IModel<List<LoginSession>> getOpenLoginSessions(IModel<User> mUser);

	/**
	 * Return a list of all LoginSessions for the given User.
	 *
	 * @param mUser Model of a User
	 * @return Model of the list of LoginSessions.
	 */
	IModel<List<LoginSession>> getAllLoginSessions(IModel<User> mUser);

	/**
	 * Get an object that will give the total number of logins
	 * and the latest login date for a particular user.  This is a
	 * (somewhat) efficient query.
	 * 
	 * @param user User to query
	 * @return LoginData object containing the requested information
	 */
	LoginData getLoginSessions(IModel<User> user);

}