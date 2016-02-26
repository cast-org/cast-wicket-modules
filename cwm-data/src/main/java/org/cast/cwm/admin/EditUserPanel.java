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
package org.cast.cwm.admin;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.databinder.auth.valid.EqualPasswordConvertedInputValidator;
import net.databinder.components.hib.DataForm;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.FormComponentContainer;
import org.cast.cwm.data.validator.UniqueUserFieldValidator;
import org.cast.cwm.data.validator.UniqueUserFieldValidator.Field;
import org.cast.cwm.service.ISiteService;
import org.cast.cwm.service.IUserService;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A panel for editing a user.  
 * 
 * TODO: Make this a little more AJAX Friendly for subclasses.
 * TODO: This does not currently work with AJAX
 * TODO: Can it ever work with AJAX?  Must re-write databinder.
 * 
 * @author jbrookover
 *
 */
@SuppressWarnings("WicketForgeJavaIdInspection")
public class EditUserPanel extends Panel {

	@Inject
	private ISiteService siteService;
	
	@Inject 
	private IUserService userService;

	@Getter
	private UserForm userForm;

	private Map<String, FormComponentContainer> components = new HashMap<String, FormComponentContainer>();
	
	@Getter @Setter
	private boolean autoConfirmNewUser = false;
	
	/**
	 * Feedback Panel for form.  Can be obtained by subclasses
	 * needing to reference it (e.g. for redrawing via Ajax)
	 */
	@Getter
	private FeedbackPanel feedbackPanel;
	
	/**
	 * Construct a panel for creating a new user.
	 * @param id wicket ID
	 */
	public EditUserPanel(String id) {
		super(id);
		addComponents(null);
	}

	/**
	 * Construct a panel for editing an existing user.
	 * 
	 * TODO: Should allow general IModel<User> instead of HibernateObjectModel,
	 * but this will require some cascading changes.
	 * 
	 * @param id wicket ID
	 * @param model model of User to edit
	 */
	public EditUserPanel(String id, HibernateObjectModel<User> model) {
		super(id, model);
		addComponents(model);
	}
	
	/**
	 * Add the normal components to this panel.
	 * @param model Model of the user to edit, if any, otherwise null.
	 */
	protected void addComponents(HibernateObjectModel<User> model) {
		userForm = getUserForm("form", model);
		add(userForm);
	}
	
	/**
	 * Get the Form component for the editing panel.
	 * @param id wicket id
	 * @return the newly-constructed Form
	 */
	protected UserForm getUserForm(String id, HibernateObjectModel<User> model) {
		UserForm form;
		if (model!=null)
			form = new UserForm(id, model);
		else
			form = new UserForm(id);

		Component submitComponent = getSubmitComponent("submit");
		if (submitComponent != null)
			form.add(submitComponent);
		
		Component cancelComponent = getCancelComponent("cancel");
		if (cancelComponent != null)
			form.add(cancelComponent);
		
		return form;
	}
	
	/**
	 * Returns a link that will be used to submit the form.
	 * This can be overridden to use a button or other component.
	 * 
	 * TODO: This "operation" thing is hidden and can cause confusion when subclasses
	 * override this method.  Should just expect a single Component, which may be a panel/fragment.
	 * 
	 * @param id The wicket ID for the component
	 * @return a Component, or null if there should be none.
	 */
	protected Component getSubmitComponent(String id) {
		SubmitLink link = new SubmitLink(id);
		link.add(new Label("operation", new AbstractReadOnlyModel<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				return userForm.getModelObject().isTransient() ? "Create User" : "Update User";
			}
		}));
		return link;
	}
	
	/**
	 * Can be used to return a link or other component that will cancel the form.
	 * By default, there is no such component, so null is returned.
	 * @param id wicket id
	 * @return newly constructed cancel button
	 */
	protected Component getCancelComponent(String id) {
		return new WebMarkupContainer(id).setVisible(false);
	}
	
	/**
	 * Returns the user model that this panel is editing.  This
	 * is useful if you need to set certain fields programmatically
	 * before the form is displayed (e.g. role).
	 * 
	 * @return model of the User
	 */
	public IModel<User> getUserModel() {
		return userForm.getModel();
	}
	
	/**
	 * Set the visibility of a field.
	 * 
	 * @param field wicket id of the field to modify
	 * @return this, for chaining
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
	 * @param field wicket id of the field to modify
	 * @return this, for chaining
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
	 * @param field wicket id of the field to modify
	 * @return this, for chaining
	 */
	public EditUserPanel setRequired(String field, boolean required) {
		FormComponentContainer component = components.get(field);
		if (component != null)
			component.setRequired(required);
		return this;
	}
	
	/**
	 * Actions to take after creation of a new user.
	 * This might be overridden, for example, to redirect to a different page.
	 * @param mUser The user just created
	 * @param trigger the component that triggered the creation
	 */
	protected void onUserCreated(IModel<User> mUser, Component trigger) {
		userService.onUserCreated(mUser, trigger);
		if (autoConfirmNewUser)
			userService.confirmUser(mUser.getObject());
	}
	
	/**
	 * Actions to take after updating a user.
	 * By default does nothing.  You could override it to redirect to a new page 
	 * or display a confirmation message.
	 * @param mUser the user just updated
	 * @param trigger the component that triggered the update
	 */
	protected void onUserUpdated(IModel<User> mUser, Component trigger) {
		userService.onUserUpdated(mUser, trigger);
	}

	/**
	 * A Hibernate DataForm for modifying or creating a user account.
	 * 
	 * @author jbrookover
	 *
	 */
	public class UserForm extends DataForm<User>{

		private static final long serialVersionUID = 1L;
		
		private PasswordTextField password; // For later conversion
		private PasswordTextField verifyPassword; // For validation

		/**
		 * Constructor.  Used for creating a new user.
		 * 
		 * @param id wicket:id
		 */
		@SuppressWarnings("unchecked")
		public UserForm(String id) {
			// FIXME I don't think this will work if userService returns something other than User.class.
			super(id, (Class<User>) userService.getUserClass());
			addFields();
		}
		
		/**
		 * Constructor.  Used for editing an existing user.
		 * 
		 * @param id wicket id
		 * @param model model of the User to edit
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
			email.add(EmailAddressValidator.getInstance());
			email.add(new UniqueUserFieldValidator(getModel(), Field.EMAIL));

			FormComponentContainer emailContainer = new FormComponentContainer("emailEnclosure", email).setLabel("Email:");
			components.put("email", emailContainer);
			add(emailContainer);
			
			// Primary Password (actually persisted and required or not)
			password = new PasswordTextField("password", new Model<String>(null));
			password.add(StringValidator.lengthBetween(4, 32));
			password.add(new PatternValidator("[\\w!@#$%^&*()=_+\\\\.,;:/-]+"));
			password.setRequired(getModelObject().isTransient());
			
			FormComponentContainer passwordContainer = new FormComponentContainer("passwordEnclosure", password).setLabel("Password:");
			components.put("password", passwordContainer);
			add(passwordContainer);
			
			// Verification Password (no validation requirements other than to be equal to password)
			verifyPassword = new PasswordTextField("verifyPassword", new Model<String>(null));
			verifyPassword.setRequired(false); // Does not need an additional error message.
			
			// Compensate for Stupid Databinder
			// add(new AttributeAppender("onsubmit", false, new Model<String>("return true;"), ";"));
			
			FormComponentContainer verifyPasswordContainer = new FormComponentContainer("verifyPasswordEnclosure", verifyPassword).setLabel("Verify Password:");
			components.put("verifyPassword", verifyPasswordContainer);
			add(verifyPasswordContainer);
			
			// Passwords have to match
			add(new EqualPasswordConvertedInputValidator(password, verifyPassword));
			
			// Permission
			CheckBox permission = new CheckBox("permission", new PropertyModel<Boolean>(this.getDefaultModel(), "permission"));
			FormComponentContainer permissionContainer = new FormComponentContainer("permissionEnclosure", permission).setLabel("Permission:");
			components.put("permission", permissionContainer);
			add(permissionContainer);

			// active
			CheckBox active = new CheckBox("valid", new PropertyModel<Boolean>(this.getDefaultModel(), "valid"));
			FormComponentContainer activeContainer = new FormComponentContainer("validEnclosure", active).setLabel("Valid:");
			components.put("valid", activeContainer);
			add(activeContainer);
			
			// Periods
			CheckBoxMultipleChoice<Period> periods = new CheckBoxMultipleChoice<Period>("periods", siteService.listPeriods());
			periods.setChoiceRenderer(new ChoiceRenderer<Period>() {

				private static final long serialVersionUID = 1L;
				
				@Override
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
			feedbackPanel = new FeedbackPanel("feedback");
			feedbackPanel.setOutputMarkupId(true);
			add(feedbackPanel);
			
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
			boolean isNewUser = getModelObject().isTransient();

			// Because we're using funky RSA passwords
			if (password.getConvertedInput() != null && !password.getConvertedInput().isEmpty())
				getModelObject().setPassword(password.getConvertedInput());
			
			super.onSubmit(); // Commit changes

			info("User '" + getModelObject().getUsername() + "' " + (isNewUser ? "Saved." : "Updated."));

			if (isNewUser)
				EditUserPanel.this.onUserCreated(getModel(), this);
			else
				EditUserPanel.this.onUserUpdated(getModel(), this);
		}
	}
}
