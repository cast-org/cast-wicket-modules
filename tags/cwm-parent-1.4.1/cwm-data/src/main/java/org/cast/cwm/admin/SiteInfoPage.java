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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.databinder.components.hib.DataForm;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
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
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.behavior.MaxLengthAttribute;
import org.cast.cwm.data.component.DeletePersistedObjectDialog;
import org.cast.cwm.data.component.FormComponentContainer;
import org.cast.cwm.data.validator.UniqueDataFieldValidator;
import org.cast.cwm.service.SiteService;
import org.cast.cwm.service.UserSpreadsheetReader;
import org.cast.cwm.service.UserSpreadsheetReader.PotentialUserSave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page for editing an individual site.  Link to periods and allows uploading files of users
 * once a given site.
 *
 */
@AuthorizeInstantiation("ADMIN")
public class SiteInfoPage extends AdminPage {
	
	private static final Logger log = LoggerFactory.getLogger(SiteInfoPage.class);

	private IModel<Site> mSite = null;

	public SiteInfoPage(PageParameters param) {
		super(param);

		// Current Site
		Long siteId = param.getAsLong("siteId");
		mSite = SiteService.get().getSiteById(siteId); // May create a Transient Instance
		if (siteId != null && mSite.getObject() == null)
			throw new RestartResponseAtInterceptPageException(SiteListPage.class);

		// Breadcrumb link
		add(new BookmarkablePageLink<Void>("siteList", SiteListPage.class));

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
		ListView<Period> list = new ListView<Period>("periodList", new PropertyModel<List<Period>>(mSite, "sitesAsSortedReadOnlyList")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Period> item) {
				item.add(new BookmarkablePageLink<Void>("periodLink", PeriodInfoPage.class)
						.setParameter("periodId", item.getModelObject().getId())
						.add(new Label("name", item.getModelObject().getName())));

				DeletePersistedObjectDialog<Period> dialog = new DeletePersistedObjectDialog<Period>("deletePeriodModal", item.getModel()) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void deleteObject() {
						SiteService.get().deletePeriod(getModel());
					}
				};
				item.add(dialog);
				item.add(new WebMarkupContainer("deletePeriodLink").add(dialog.getDialogBorder().getClickToOpenBehavior()));
			}
		};
		add(list);

		// Link to create a new Period
		BookmarkablePageLink<Void> createNewPeriod = new BookmarkablePageLink<Void>("createNewPeriod", PeriodInfoPage.class);
		if (siteId != null)
			createNewPeriod.setParameter("siteId", siteId);
		createNewPeriod.setVisible(!mSite.getObject().isTransient()); // For Enclosure Visibility
		add(createNewPeriod);

		// Sample CSV File
		add(new ResourceLink<Void>("sampleLink", new ResourceReference(SiteInfoPage.class, "sample_class_list.csv")));
		
		// Upload CSV File to populate Site
		Folder uploadFolder = new Folder(System.getProperty("java.io.tmpdir"), "wicket-uploads");
		uploadFolder.mkdirs();
		add(new UserFileUploadForm("upload-form", mSite, uploadFolder));
		
		
	}

	/**
	 * Form for modifying Site details.
	 * 
	 * @author jbrookover
	 *
	 */
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

		private UserSpreadsheetReader reader;
		private WebMarkupContainer resultsContainer = new WebMarkupContainer("results-container");
		private Label uploadResults = new Label("upload-results", "UploadedData");
		private Folder uploadFolder;


		public UserFileUploadForm(String id, IModel<Site> site, Folder uploadFolder) {
			super(id, site);
			
			this.uploadFolder = uploadFolder;

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
					reader.save();
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
				// Create a new file
				File newFile = new File(uploadFolder, upload.getClientFileName());
				if(newFile.exists()) {
					if(!Files.remove(newFile)) {
						throw new IllegalStateException("File already exists and can't be deleted");
					}
				}
				try {
					// Save to new file
					newFile.createNewFile();
					upload.writeTo(newFile);
				}
				catch (Exception e) {
					throw new IllegalStateException("Unable to write file");
				}

				reader = new UserSpreadsheetReader();
				reader.setDefaultSite(getModel());
				boolean success;
				try {
					success = reader.readInput(upload.getInputStream());
				} catch (IOException e) {
					success = false;
				}

				if (!success) {
					if (reader.getGlobalError() != null)
						error(reader.getGlobalError());
					for (PotentialUserSave pus : reader.getPotentialUsers()) {
						if (!pus.getError().equals("")) {
							String[] errors = pus.getError().split("\n");
							for (int i = 0; i < errors.length; i++) {
								error("Error on line " + pus.getLine() + ": " + errors[i]);
							}
						}
					}
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

			List<String> headers = new ArrayList<String>();
			headers.add("Line");  headers.add("SubjectID"); headers.add("Type"); 
			headers.add("Period"); headers.add("Username"); headers.add("Password"); 
			headers.add("Full Name"); headers.add("Email");

			resultsContainer.add(new ListView<String>("headers", headers) {

				private static final long serialVersionUID = 1L;

				protected void populateItem(ListItem<String> item) {
					String value = (String) item.getModelObject();
					item.add(new Label("value", value));
				}
			});

			resultsContainer.add(new ListView<PotentialUserSave>("data-rows", reader.getPotentialUsers()) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<PotentialUserSave> item) {

					PotentialUserSave pe = item.getModelObject();
					User p = pe.getUser().getObject();
					List<String> values = new ArrayList<String>();
					values.add(String.valueOf(pe.getLine())); values.add(p.getSubjectId());
					values.add(p.getRole().toString()); 
					values.add(p.getPeriods().iterator().next().getName()); 
					values.add(p.getUsername()); values.add("********");
					values.add(p.getFullName());
					values.add(p.getEmail());
					item.add(new ListView<String>("data-columns", values) {

						private static final long serialVersionUID = 1L;

						@Override
						protected void populateItem(ListItem<String> item) {
							String value = (String) item.getModelObject();
							item.add(new Label("value", value));
						}

					});
					if (!pe.getError().matches("")) {
						item.add(new AttributeAppender("style", true, new Model<String>("color:red"), ";"));
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
