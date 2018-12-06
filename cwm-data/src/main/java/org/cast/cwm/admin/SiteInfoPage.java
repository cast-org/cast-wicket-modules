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
package org.cast.cwm.admin;

import com.google.inject.Inject;
import net.databinder.components.hib.DataForm;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.behavior.MaxLengthAttribute;
import org.cast.cwm.data.component.FormComponentContainer;
import org.cast.cwm.data.models.UserPeriodNamesModel;
import org.cast.cwm.data.validator.UniqueDataFieldValidator;
import org.cast.cwm.figuration.hideable.ConfirmationModal;
import org.cast.cwm.figuration.hideable.FigurationTriggerBehavior;
import org.cast.cwm.service.IAdminPageService;
import org.cast.cwm.service.ISiteService;
import org.cast.cwm.service.ISpreadsheetReader;
import org.cast.cwm.service.UserSpreadsheetReader.PotentialUserSave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Page for editing an individual site.  Link to periods and allows uploading files of users
 * once a given site.
 *
 */
@AuthorizeInstantiation("ADMIN")
public class SiteInfoPage extends AdminPage {

	private static final Logger log = LoggerFactory.getLogger(SiteInfoPage.class);

	private IModel<Site> mSite = null;
	
	@Inject
	private ISiteService siteService;

	@Inject
	private IAdminPageService adminPageService;

	public SiteInfoPage(PageParameters param) {
		super(param);

		// Current Site
		Long siteId = param.get("siteId").toOptionalLong();
		mSite = siteService.getSiteById(siteId); // May create a Transient Instance
		if (siteId != null && mSite.getObject() == null)
			throw new RestartResponseAtInterceptPageException(adminPageService.getSiteListPage());

		// Breadcrumb link
		add(adminPageService.getSiteListPageLink("siteList"));

		// Directions (Create New vs Existing)
		add(new Label("instructions", new AbstractReadOnlyModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override

			public String getObject() {
				Site s = mSite.getObject();
				return (s == null ? "Create New Site" : "Edit Site: " + s.getName());
			}
		}));

		add(new SiteForm("form", mSite));

		// List Existing Periods
		ListView<Period> list = new ListView<Period>("periodList", new PropertyModel<List<Period>>(mSite, "periodsAsSortedReadOnlyList")) {

			@Override
			protected void populateItem(ListItem<Period> item) {
				Link link = adminPageService.getPeriodEditPageLink("periodLink", item.getModel());
				item.add(link);
				link.add(new Label("name", item.getModelObject().getName()));

				ConfirmationModal<Period> deleteDialog = new ConfirmationModal<Period>("deletePeriodModal", item.getModel()) {

					@Override
					protected boolean onConfirm(AjaxRequestTarget target) {
						siteService.deletePeriod(getModel());
						target.add(SiteInfoPage.this);
						return true;
					}
				};
				item.add(deleteDialog);
				item.add(new WebMarkupContainer("deletePeriodLink").add(new FigurationTriggerBehavior(deleteDialog)));
			}
		};
		add(list);

		// Link to create a new Period
		Link createNewPeriod = adminPageService.getNewPeriodEditPageLink("createNewPeriod", mSite);
		createNewPeriod.setVisible(!mSite.getObject().isTransient()); // For Enclosure Visibility
		add(createNewPeriod);

		// Sample CSV File
		add(new ResourceLink<Void>("sampleLink", new PackageResourceReference(SiteInfoPage.class, "sample_class_list.csv")));
		
		// Upload CSV File to populate Site
		add(new UserFileUploadForm("upload-form", mSite));
	}

	/**
	 * Form for modifying Site details.
	 * 
	 * @author jbrookover
	 *
	 */
	@SuppressWarnings("WicketForgeJavaIdInspection")
	private class SiteForm extends DataForm<Site> {

		private static final long serialVersionUID = 1L;

		public SiteForm(String id, IModel<Site> model) {
			super(id, (HibernateObjectModel<Site>) model);

			// Default Values
			if (getModelObject().isTransient()) {
				getModelObject().setLanguage("en");
				getModelObject().setCountry("us");
				getModelObject().setTimezone("America/New_York");
			}

			// Feedback (errors and status update)
			add(new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this)));

			// Site Name
			TextField<String> name = new TextField<String>("name");
			name.setRequired(true);
			name.add(StringValidator.lengthBetween(1, 32));
			name.add(new MaxLengthAttribute(32));
			name.add(new PatternValidator("[\\w!@#$%^&*()=_+;:/ -]+"));
			name.add(new UniqueDataFieldValidator<String>(getModel(), "name"));

			FormComponentContainer nameContainer = new FormComponentContainer("nameEnclosure", name).setLabel("Site Name:");
			add(nameContainer);

			// Site Anonymous ID
			TextField<String> siteId = new TextField<String>("siteId");
			siteId.setRequired(true);
			siteId.add(StringValidator.lengthBetween(1, 32));
			siteId.add(new MaxLengthAttribute(32));
			siteId.add(new PatternValidator("[\\w!@#$%^&*()=_+;:/ -]+"));
			siteId.add(new UniqueDataFieldValidator<String>(getModel(), "siteId"));

			FormComponentContainer siteIdContainer = new FormComponentContainer("siteIdEnclosure", siteId).setLabel("Anonymous Site ID:");
			add(siteIdContainer);

			// Location
			TextField<String> location = new TextField<String>("location");
			location.add(StringValidator.lengthBetween(0, 32));
			location.add(new MaxLengthAttribute(32));
			
			FormComponentContainer locationContainer = new FormComponentContainer("locationEnclosure", location).setLabel("Location:");
			add(locationContainer);
			
			// Language
			TextField<String> language = new TextField<String>("language");
			language.setRequired(true);
			language.add(StringValidator.exactLength(2));
			language.add(new MaxLengthAttribute(2));

			FormComponentContainer languageContainer = new FormComponentContainer("languageEnclosure", language).setLabel("Language:");
			add(languageContainer);
			
			// Country
			TextField<String> country = new TextField<String>("country");
			country.setRequired(true);
			country.add(StringValidator.exactLength(2));
			country.add(new MaxLengthAttribute(2));
			
			FormComponentContainer countryContainer = new FormComponentContainer("countryEnclosure", country).setLabel("Country:");
			add(countryContainer);
			
			// Time zone
			TextField<String> timezone = new TextField<String>("timezone");
			timezone.add(StringValidator.lengthBetween(0, 32));
			timezone.add(new MaxLengthAttribute(32));
			
			FormComponentContainer timezoneContainer = new FormComponentContainer("timezoneEnclosure", timezone).setLabel("Time Zone:");
			add(timezoneContainer);
			
		}

		@Override
		protected void onSubmit() {
			String message = getModelObject().isTransient() ? "Saved." : "Updated.";
			super.onSubmit();
			info("Site '" + getModelObject().getName() + "' " + message);
		}		
	}

	/**
	 * Form for uploading a spreadsheet of users.
	 * 
	 * TODO: Clean up
	 * TODO: Remove <wicket:enclosure> tags since they shouldn't contain form elements.
	 * 
	 * @author jbrookover
	 *
	 */
	private class UserFileUploadForm extends Form<Site> {

		private static final long serialVersionUID = 1L;
		private FileUploadField file;
		private Button commitButton;
		private Button cancelButton;

		private ISpreadsheetReader reader;
		private WebMarkupContainer resultsContainer = new WebMarkupContainer("results-container");
		private Label uploadResults = new Label("upload-results", "UploadedData");

		public UserFileUploadForm(String id, IModel<Site> site) {
			super(id, site);

			setMultiPart(true);
			setMaxSize(Bytes.megabytes(10));
			
			// Upload Field
			add(file = new FileUploadField("upload-file"));
			
			// Upload Field Label
			add(new FormComponentLabel("uploadLabel", file));

			ContainerFeedbackMessageFilter filter = new ContainerFeedbackMessageFilter(this);
			FeedbackPanel feedback = new FeedbackPanel("upload-feedback", filter);
			add(feedback);

			commitButton = new Button("commit-button") {

				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit() {				
					reader.save(this);
					info("Users Submitted!");
					log.info("User list saved to database");
					commitButton.setVisible(false);
					file.setVisible(true);
					clearResults();
				}			
			};
			
			commitButton.setDefaultFormProcessing(false); // Not sure what this is...
			commitButton.setVisible(false);
			add(commitButton);

			cancelButton = new Button("cancel-button") {

				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit() {
					log.debug("Cancel Button Pressed");
					reader = null;
					commitButton.setVisible(false);
					file.setVisible(true);
					clearResults();
				}

			};
			cancelButton.setDefaultFormProcessing(false);
			add(cancelButton);

			add(uploadResults);
			add(resultsContainer);
			clearResults();
			setEscapeModelStrings(false);
		}

		public FileUpload getFileUpload() { 
			return file.getFileUpload(); 
		}

		@Override
		protected void onSubmit() {
			clearResults();
			final FileUpload upload = getFileUpload();
			if (upload != null) {
				reader = adminPageService.getUserSpreadsheetReader();
				reader.setDefaultSite(getModel());
				boolean success;
				try {
					success = reader.readInput(upload.getInputStream());
				} catch (IOException e) {
                    e.printStackTrace();
					success = false;
				}

				if (!success) {
					if (reader.getGlobalError() != null)
						error(reader.getGlobalError());
					else
						error("One or more errors in file, see below");
				}
				displayResults();

				if(success) {
					log.debug("Ready to commit");
					file.setVisible(false);
					commitButton.setVisible(true);
				} 

			} else {
				error("Please select a student list to upload. \n");
			}
		}

		private void clearResults() {
			resultsContainer.removeAll();
			uploadResults.setVisible(false);
		}

		private void displayResults() {

			clearResults();

			uploadResults.setVisible(true);

			resultsContainer.add(new ListView<PotentialUserSave>("data-rows", reader.getPotentialUsers()) {

				@Override
				protected void populateItem(ListItem<PotentialUserSave> item) {
					IModel<PotentialUserSave> mp = item.getModel();
					IModel<User> mu = item.getModelObject().getUser();

					item.add(new Label("line", new PropertyModel<Integer>(mp, "csvRecord.recordNumber")));
					item.add(new Label("subjectId", new PropertyModel<Integer>(mu, "subjectId")));
					item.add(new Label("role", new PropertyModel<Integer>(mu, "role")));
					item.add(new Label("periods", new UserPeriodNamesModel(mu)));
					item.add(new Label("username", new PropertyModel<Integer>(mu, "username")));
					item.add(new Label("fullname", new PropertyModel<Integer>(mu, "fullName")));
					item.add(new Label("email", new PropertyModel<Integer>(mu, "email")));
					item.add(new Label("permission", new PropertyModel<Integer>(mu, "permission")));
					item.add(new Label("error", new PropertyModel<String>(mp, "error")));


					if (!mp.getObject().getError().matches("")) {
						item.add(new AttributeAppender("style", new Model<String>("color:red"), ";"));
					}
				}
			});
		}

		@Override
		public boolean isVisible() {
			return !getModelObject().isTransient();
		}
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		if (mSite != null)
			mSite.detach();
	}


}
