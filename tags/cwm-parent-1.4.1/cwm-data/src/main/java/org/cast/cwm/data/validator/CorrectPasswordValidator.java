/*
 * Copyright 2011 CAST, Inc.
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

import net.databinder.auth.hib.AuthDataSession;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;

/**
 * Validate that the password given actually belongs to the logged-in user.
 * Generally used for "Change Password" pages or other pages that require 
 * greater than the usual security.
 * 
 * @author bgoldowsky
 */
public class CorrectPasswordValidator extends AbstractValidator<String> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void onValidate(IValidatable<String> validatable) {		
		if (!AuthDataSession.get().getUser().getPassword().matches(validatable.getValue())) {
			error(validatable);
		}
	}

}
