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
package org.cast.cwm.data.validator;

import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.cast.cwm.data.User;
import org.cast.cwm.service.UserService;

/**
 * Validate that the username is not already in use in the database by a different
 * {@link User}.  This is constructed with a given user for comparison (e.g. of course
 * an existing user is using their own username).
 * 
 * This should for example be attached to a username field of a registration form. 
 * 
 * @author bgoldowsky
 */
public class UniqueUserFieldValidator extends AbstractValidator<String> {
	
	public static enum Field {USERNAME, EMAIL, SUBJECTID };

	private static final long serialVersionUID = 1L;
	private IModel<User> currentUser;
	private Field field;
	
	public UniqueUserFieldValidator(Field field) {
		this(new Model<User>(null), field);
	}
	
	public UniqueUserFieldValidator(IModel<User> currentUser, Field field) {
		this.currentUser = currentUser;
		this.field = field;
	}

	@Override
	protected void onValidate(IValidatable<String> validatable) {

		IModel<User> other;
		
		switch(field) {
		case USERNAME:
			other = UserService.get().getByUsername(validatable.getValue());
			break;
		case EMAIL:
			other = UserService.get().getByEmail(validatable.getValue());
			break;
		case SUBJECTID:
			other = UserService.get().getBySubjectId(validatable.getValue());
			break;
		default:
			throw new IllegalArgumentException("Invalid User Field");
		}

		if (other.getObject() != null && !other.getObject().equals(currentUser.getObject())) {
			error(validatable);
		}
	}
	
	@Override 
	protected Map<String, Object> variablesMap(IValidatable<String> validatable) {
		Map<String, Object> map = super.variablesMap(validatable);
		map.put("field", field.toString().toLowerCase());
		return map;
	}

}
