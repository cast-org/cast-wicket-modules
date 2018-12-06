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

import com.google.inject.Inject;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.cast.cwm.data.User;
import org.cast.cwm.service.IUserService;

/**
 * Validate that the username is not already in use in the database by a different
 * {@link User}.  This is constructed with a given user for comparison (e.g. of course
 * an existing user is using their own username).
 * 
 * This should for example be attached to a username field of a registration form. 
 * 
 * @author bgoldowsky
 */
public class UniqueUserFieldValidator implements IValidator<String> {
	
	public enum Field {USERNAME, EMAIL, SUBJECTID }

	private IModel<? extends User> currentUser;
	private Field field;
	
	@Inject 
	protected IUserService userService;
	
	public UniqueUserFieldValidator(Field field) {
		this(new Model<User>(null), field);
	}
	
	public UniqueUserFieldValidator(IModel<? extends User> currentUser, Field field) {
		this.currentUser = currentUser;
		this.field = field;
		Injector.get().inject(this);
	}

	@Override
	public void validate(IValidatable<String> validatable) {

		IModel<? extends User> other;
		
		switch(field) {
		case USERNAME:
			other = userService.getByUsername(validatable.getValue());
			break;
		case EMAIL:
			other = userService.getByEmail(validatable.getValue());
			break;
		case SUBJECTID:
			other = userService.getBySubjectId(validatable.getValue());
			break;
		default:
			throw new IllegalArgumentException("Invalid User Field");
		}

		if (other.getObject() != null && !other.getObject().equals(currentUser.getObject())) {
			ValidationError error = new ValidationError(this);
			error.setVariable("field", field.toString().toLowerCase());
			validatable.error(error);
		}
	}

}
