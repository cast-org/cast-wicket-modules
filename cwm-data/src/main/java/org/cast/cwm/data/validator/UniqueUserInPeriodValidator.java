/*
 * Copyright 2011-2014 CAST, Inc.
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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IUserService;

/**
 * A form-level validator to make sure the combination of first and last name in a form are unique within the current Period.
 * Implementor must define methods to return the form components for the first and last names.
 * The User object is expected to be the model of the form; if not, override the getUser() method.
 *
 * @author jbrookover
 *
 */
public abstract class UniqueUserInPeriodValidator extends AbstractFormValidator {
	
	@Inject
	protected IUserService userService;

	@Inject
	protected ICwmSessionService cwmSessionService;

	private static final long serialVersionUID = 1L;
	
	public UniqueUserInPeriodValidator() {
		Injector.get().inject(this);
	}
	
	@Override
	public void validate(Form<?> form) {
		
		// Ensure that no other users exist in this period with the same full name
		User student = getUser(form);

		String firstName = getFirstNameComponent().getValue();
		String lastName = getLastNameComponent().getValue();
		IModel<User> otherUser = userService.getByFullnameFromPeriod(firstName, lastName,
				cwmSessionService.getCurrentPeriodModel());
		if ((otherUser != null) && (otherUser.getObject() != null)
				&& !student.equals(otherUser.getObject())) {
			error(getFirstNameComponent(), "UniqueUserInPeriodValidator");
		}
	}

	protected User getUser(Form<?> form) {
		return (User) form.getModelObject();
	}

	@Override
	public FormComponent<?>[] getDependentFormComponents() {
		return new FormComponent<?>[] { getFirstNameComponent(), getLastNameComponent() };
	}
	
	public abstract FormComponent<String> getFirstNameComponent();
	public abstract FormComponent<String> getLastNameComponent();
	
	/**
	 * A helper function that uses an IVisitor to find a TextField<String> in a provided
	 * form with the given wicketId.  Someday, findChild will exist.
	 * 
	 * @param form the Form to search within
	 * @param wicketId wicket ID to search for
	 * @return the field, or null if not found
	 */
	public static TextField<String> findFieldInForm(Form<?> form, final String wicketId) {
		return form.visitChildren(TextField.class, new IVisitor<TextField<String>, TextField<String>>() {
			@Override
			public void component(TextField<String> component, IVisit<TextField<String>> visit) {
				if (component.getId().equals(wicketId)) {
					visit.stop(component);
				}
			}
		});
	}
}
