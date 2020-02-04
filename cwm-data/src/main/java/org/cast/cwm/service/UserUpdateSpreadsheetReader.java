/*
 * Copyright 2011-2020 CAST, Inc.
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

import com.google.inject.Inject;
import org.apache.commons.csv.CSVRecord;
import org.apache.wicket.Component;
import org.cast.cwm.data.User;

import java.util.Map;

/**
 * Read a spreadsheet of user data and update user account information.
 *
 * Spreadsheet must have either a "username" or "subjectid" column to identify which accounts to update.
 * If both are provided, then username identifies accounts and subjectIds are updated to the values in the
 * spreadsheet.
 *
 * Other understood columns are firstname, lastname, password, email, and permission.
 *
 * @author bgoldowsky
 */
public class UserUpdateSpreadsheetReader extends UserSpreadsheetReader {

	@Inject
	private IUserService userService;

	@Inject
	private ICwmService cwmService;

	String keyField = "username";

	public UserUpdateSpreadsheetReader() {
		super();
	}

	@Override
	protected String checkRequiredHeaders(Map<String, Integer> map) {
		// Either a column of usernames or a column of subjectIds must be supplied, for user identification.
		// If both are supplied, username is assumed to be the "real" identifier.
		String message = "";
		if (!map.containsKey("username")) {
			if (map.containsKey("subjectid"))
				keyField = "subjectid";
			else
				message += "Either a 'username' column or a 'subjectid' column must be included.";
		}
		return message;
	}

	@Override
	protected User createUserObject(CSVRecord record) {
		User user;
		if (keyField.equals("username")) {
			user = userService.getByUsername(get(record, "username")).getObject();
		} else {
			user = userService.getBySubjectId(get(record, "subjectid")).getObject();
		}
		return user;
	}

	@Override
	protected String populateUserObject(User user, CSVRecord record) {
		// The created user object may be null, if user wasn't found.
		if (user == null) {
			return String.format("User with %s=%s not found", keyField, get(record, keyField));
		}

		// Set Names
		if (notEmpty(record, "firstname"))
			user.setFirstName(get(record, "firstname"));

		if (notEmpty(record, "lastname"))
			user.setLastName(get(record, "lastname"));

		// Set Permission
		if (notEmpty(record, "permission")) {
			boolean permission = false;
			String value = get(record, "permission").trim().toLowerCase();
			if (value.equals("true") || value.equals("1") ) {
				permission = true;
			}
			user.setPermission(permission);
		}

		// Set Password
		if(notEmpty(record, "password")) {
			user.setPassword(get(record, "password"));
		}

		// Set Username
		if(!keyField.equals("username") && notEmpty(record, "username")) {
			user.setUsername(get(record, "username"));
		}

		// Set SubjectId (Default to Username)
		if(!keyField.equals("subjectid") && notEmpty(record, "subjectid")) {
			user.setSubjectId(get(record, "subjectid"));
		}

		// Set email
		if(notEmpty(record, "email")) {
			user.setEmail(get(record, "email"));
		}

		return "";
	}

	protected String validateUser(User user) {
		String messages = "";

		// Make sure user exists
		if (user == null)
			return "Failed to look up user by " + keyField;

		// Check database for duplicate subjectId (if subjectId is not the key field)
		if (!keyField.equals("subjectid")) {
			User other = userService.getBySubjectId(user.getSubjectId()).getObject();
			if (other != null && !other.equals(user))
				messages += "SubjectId " + user.getSubjectId() + " already exists in database.\n";
		}

		// Check database for duplicate email addresses when an email address exists
		if (user.getEmail() != null) {
			User other = userService.getByEmail(user.getEmail()).getObject();
			if (other != null && !other.equals(user))
				messages += "Email " + user.getEmail() + " already exists in database.\n";
		}

		// Check uploaded user list for duplicate username, subjectId, "Full Name"
		messages += checkForListDuplicates(user);

		return messages;
	}

	@Override
	public void save(Component triggerComponent) {
		// Since User models have been detached and reattached, changes made during validation are lost.
		// Need to reapply them (and anyway, User might have changed in the DB since
		// the previous page view, so it's not unreasonable).
		for(PotentialUserSave potentialUser : potentialUsers) {
			populateUserObject(potentialUser.getUser().getObject(), potentialUser.getCsvRecord());
			userService.onUserUpdated(potentialUser.getUser(), triggerComponent);
		}
		cwmService.flushChanges();
	}

}
