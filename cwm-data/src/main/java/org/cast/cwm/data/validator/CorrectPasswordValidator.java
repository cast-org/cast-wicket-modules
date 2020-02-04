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
package org.cast.cwm.data.validator;

import net.databinder.auth.AuthDataSessionBase;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Validate that the password given actually belongs to the logged-in user.
 * Generally used for "Change Password" pages or other pages that require 
 * greater than the usual security.
 * 
 * @author bgoldowsky
 */
public class CorrectPasswordValidator implements IValidator<String> {

	@Override
	public void validate(IValidatable<String> validatable) {
		if (!AuthDataSessionBase.get().getUser().getPassword().matches(validatable.getValue())) {
			validatable.error(new ValidationError(this));
		}
	}
}
