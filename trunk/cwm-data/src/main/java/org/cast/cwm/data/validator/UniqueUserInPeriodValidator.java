/*
 * Copyright 2011-2013 CAST, Inc.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.User;
import org.cast.cwm.service.UserService;

/**
 * A hacked together validator to ensure that no two users exist in the same period
 * with the same name.  Assumes ISISession.get().getCurrentPeriodModel() since there
 * is no detaching of Form Validators.
 * 
 * @author jbrookover
 *
 */
public abstract class UniqueUserInPeriodValidator extends AbstractFormValidator {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void validate(Form<?> form) {
		
		// Ensure that no other users exist in this period with the same full name
		User student = (User) form.getModelObject();
		
		IModel<User> otherUser = UserService.get().getByFullnameFromPeriod(getFirstNameComponent().getConvertedInput(), getLastNameComponent().getConvertedInput(), CwmSession.get().getCurrentPeriodModel());
		if (otherUser.getObject() != null && !student.equals(otherUser.getObject())) {
			error(getFirstNameComponent(), "UniqueUserInPeriodValidator");
			return;
		}
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
	 * @param form
	 * @param wicketId
	 * @return
	 */
	public static TextField<String> findFieldInForm(Form<?> form, final String wicketId) {
		final List<TextField<String>> list = new ArrayList<TextField<String>>();
		form.visitChildren(TextField.class, new IVisitor<TextField<String>,Void>() {

			@Override
			public void component(TextField<String> component, IVisit<Void> visit) {
				if (component.getId().equals(wicketId)) {
					list.add(component);
					visit.stop();
				}
			}
			
		});
		return list.get(0);
	}
}
