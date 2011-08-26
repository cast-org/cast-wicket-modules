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
package org.cast.cwm.admin;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import net.databinder.auth.components.RSAPasswordTextField;
import net.databinder.auth.valid.EqualPasswordConvertedInputValidator;
import net.databinder.components.hib.DataForm;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.FormComponentContainer;
import org.cast.cwm.data.models.UserModel;
import org.cast.cwm.data.validator.UniqueUserFieldValidator;
import org.cast.cwm.data.validator.UniqueUserFieldValidator.Field;
import org.cast.cwm.service.SiteService;
import org.cast.cwm.service.UserService;

/**
 * A panel for editing a user.  
 * 
 * @author jbrookover
 *
 */
public class EditUserPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private Form<User> userForm;
	private Map<String, FormComponentContainer> components = new HashMap<String, FormComponentContainer>();
	
	@Getter @Setter
	private boolean autoConfirmNewUser = false;
	
	/**
	 * Construct a panel for creating a new user of the given role.
	 * @param id
	 * @param role
	 */
	public EditUserPanel(String id) {
		super(id);
		
		userForm = new UserForm("form");
		userForm.add(getSubmitButton("submitButton"));
		add(userForm);
	}
	
	/**
	 * Construct a panel for editing an existing user.
	 * @param id
	 * @param model
	 */
	public EditUserPanel(String id, IModel<User> model) {
		super(id, model);
		
		
		userForm = new UserForm("form", (UserModel) model);
		userForm.add(getSubmitButton("submitButton"));
		add(userForm);
	}
	
	protected Button getSubmitButton(String id) {
		return new Button(id, new AbstractReadOnlyModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return userForm.getModelObject().isTransient() ? "Create User" : "Update User";
			}
			
		});
	}
	
	
	/**
	 * Returns the user model that this panel is editing.  This
	 * is useful if you need to set certain fields programmatically
	 * before the form is displayed (e.g. role).
	 * 
	 * @return
	 */
	public IModel<User> getUserModel() {
		return userForm.getModel();
	}
	
	/**
	 * Set the visibility of a field.
	 * 
	 * @param field
	 * @return
	 */
	public EditUserPanel setVisible(String field, boolean visible) {
		FormComponentContainer component = components.get(field);
		if (component != null)
			component.setVisible(visible);
		return this;
	}
	
	/**
	 * Set whether a field is enabled.  This does 
	 * not affect visibility.
	 * 
	 * @param field
	 * @return
	 */
	public EditUserPanel setEnabled(String field, boolean enabled) {
		FormComponentContainer component = components.get(field);
		if (component != null)
			component.setEnabled(enabled);
		return this;
	}
	
	/**
	 * Set whether a field is required.  This does not check
	 * for any database requirements.  Therefore, setting a field
	 * to NOT be required should be done with caution.
	 * 
	 * @param field
	 * @return
	 */
	public EditUserPanel setRequired(String field, boolean required) {
		FormComponentContainer component = components.get(field);
		if (component != null)
			component.setRequired(required);
		return this;
	}
	
	/**
	 * A Hibernate DataForm for modifying or creating a user account.
	 * 
	 * @author jbrookover
	 *
	 */
	private class UserForm extends DataForm<User>{

		private static final long serialVersionUID = 1L;
		
		private RSAPasswordTextField password; // For later conversion
		private RSAPasswordTextField verifyPassword; // For validation

		/**
		 * Constructor.  Used for creating a new user.
		 * 
		 * @param id wicket:id
		 */
		@SuppressWarnings("unchecked")
		public UserForm(String id) {
			super(id, (Class<User>) UserService.get().getUserClass());
			addFields();
		}
		
		/**
		 * Constructor.  Used for editing an existing user.
		 * 
		 * @param id
		 * @param model
		 */
		public UserForm(String id, HibernateObjectModel<User> model) {
			super(id, model);
			addFields();
		}

		private void addFields() {
			
			// Role
			DropDownChoice<Role> role = new DropDownChoice<Role>("role", Arrays.asList(Role.values()));
			
			FormComponentContainer roleContainer = new FormComponentContainer("roleEnclosure", role).setLabel("Role:");
			components.put("role", roleContainer);
			add(roleContainer);
			
			// Username
			TextField<String> username = new TextField<String>("username");
			username.setRequired(true);
			username.add(StringValidator.lengthBetween(1, 32));
			username.add(new PatternValidator("[\\w.-]+"));
			username.add(new UniqueUserFieldValidator(getModel(), Field.USERNAME));	
			
			FormComponentContainer usernameContainer = new FormComponentContainer("usernameEnclosure", username).setLabel("User name:");
			components.put("username", usernameContainer);
			add(usernameContainer);
			
			// First Name
			TextField<String> firstName = new TextField<String>("firstName");
			firstName.setRequired(true);
			firstName.add(StringValidator.lengthBetween(1, 32));
			
			FormComponentContainer firstNameContainer = new FormComponentContainer("firstNameEnclosure", firstName).setLabel("First name:");
			components.put("firstName", firstNameContainer);
			add(firstNameContainer);
			
			// Last Name
			TextField<String> lastName = new TextField<String>("lastName");
			lastName.setRequired(true);
			lastName.add(StringValidator.lengthBetween(1, 32));
			
			FormComponentContainer lastNameContainer = new FormComponentContainer("lastNameEnclosure", lastName).setLabel("Last name:");
			components.put("lastName", lastNameContainer);
			add(lastNameContainer);

			// Subject ID (for Research)
			TextField<String> subjectId = new TextField<String>("subjectId");
			subjectId.setRequired(true);
			subjectId.add(StringValidator.lengthBetween(1, 32));
			subjectId.add(new UniqueUserFieldValidator(getModel(), Field.SUBJECTID));
			
			FormComponentContainer subjectIdContainer = new FormComponentContainer("subjectIdEnclosure", subjectId).setLabel("Subject ID:");
			components.put("subjectId", subjectIdContainer);
			add(subjectIdContainer);

			// E-mail Address
			TextField<String> email = new TextField<String>("email");
			email.setRequired(true);
			email.add(EmailAddressValidator.getInstance());
			email.add(new UniqueUserFieldValidator(getModel(), Field.EMAIL));

			FormComponentContainer emailContainer = new FormComponentContainer("emailEnclosure", email).setLabel("Email:");
			components.put("email", emailContainer);
			add(emailContainer);
			
			// Primary Password (actually persisted and required or not)
			password = new RSAPasswordTextField("password", new Model<String>(null), this);
			password.add(StringValidator.lengthBetween(4, 32));
			password.add(new PatternValidator("[\\w!@#$%^&*()=_+\\\\.,;:/-]+"));
			password.setRequired(getModelObject().isTransient());
			
			FormComponentContainer passwordContainer = new FormComponentContainer("passwordEnclosure", password).setLabel("Password:");
			components.put("password", passwordContainer);
			add(passwordContainer);
			
			// Verification Password (no validation requirements other than to be equal to password)
			verifyPassword = new RSAPasswordTextField("verifyPassword", new Model<String>(null), this);
			verifyPassword.setRequired(false); // Does not need an additional error message.
			
			FormComponentContainer verifyPasswordContainer = new FormComponentContainer("verifyPasswordEnclosure", verifyPassword).setLabel("Verify Password:");
			components.put("verifyPassword", verifyPasswordContainer);
			add(verifyPasswordContainer);
			
			// Passwords have to match
			add(new EqualPasswordConvertedInputValidator(password, verifyPassword));
			
			// Periods
			CheckBoxMultipleChoice<Period> periods = new CheckBoxMultipleChoice<Period>("periods", SiteService.get().listPeriods());
			periods.setChoiceRenderer(new ChoiceRenderer<Period>() {

				private static final long serialVersionUID = 1L;
				
				public Object getDisplayValue(Period object) {
					return "<strong>" + object.getName() + "</strong>" + " (Site: " + object.getSite().getName() + ")";
				}
				
			});
			periods.setVisible(periods.getChoices().size() > 0 && getModelObject().usesPeriods());
			periods.setEscapeModelStrings(false);
			
			FormComponentContainer periodsContainer = new FormComponentContainer("periodsEnclosure", periods).setLabel("Periods:");
			components.put("periods", periodsContainer);
			add(periodsContainer);
			
			// Feedback for validators
			add(new FeedbackPanel("feedback"));
			
		}

		@Override
		protected void onBeforeSave(HibernateObjectModel<User> model) {
			User user = model.getObject();
			
			// For Initial User Creation
			if (user.isTransient()) {
				user.setCreateDate(new Date());
				user.generateSecurityToken();
				if (user.getSubjectId() == null)
					user.setSubjectId(user.getUsername());
			}	
		}
		
		@Override
		protected void onSubmit() {
			
			String message = getModelObject().isTransient() ? "Saved." : "Updated.";
			
			// Because we're using funky RSA passwords
			if (password.getConvertedInput() != null && !password.getConvertedInput().isEmpty())
				getModelObject().setPassword(password.getConvertedInput());
			
			super.onSubmit(); // Commit changes
			
			if (autoConfirmNewUser)
				UserService.get().confirmUser(getModelObject());
			
			info("User '" + getModelObject().getUsername() + "' " + message);
		}
	}
}
