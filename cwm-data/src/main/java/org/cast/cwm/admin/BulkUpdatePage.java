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
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.UserPeriodNamesModel;
import org.cast.cwm.service.IAdminPageService;
import org.cast.cwm.service.ISpreadsheetReader;
import org.cast.cwm.service.UserSpreadsheetReader;
import org.cwm.db.service.IModelProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Page allowing the upload of a spreadsheet with user modifications.
 * This can be used, for example, to set SubjectIds and permisisons for all users in an experiment.
 *
 * @author bgoldowsky
 */
@AuthorizeInstantiation("ADMIN")
@Slf4j
public class BulkUpdatePage extends AdminPage {

	@Inject
	private IAdminPageService adminPageService;
	private ISpreadsheetReader reader;

	private enum DataStatus { EMPTY, INVALID, VALID }
	private DataStatus status = DataStatus.EMPTY;

	public BulkUpdatePage(PageParameters parameters) {
		super(parameters);

		// Upload CSV File to populate Site
		add(new UpdateUploadForm("upload-form"));
	}

	List<UserSpreadsheetReader.PotentialUserSave> getPotentialUserList() {
		if (reader != null)
			return reader.getPotentialUsers();
		else
			return Collections.emptyList();
	}


	protected class UpdateUploadForm extends Form<Void> {

		private final ConfirmationDisplay confirmationDisplay;
		private FileUploadField uploadField;

		protected UpdateUploadForm(String wicketId) {
			super(wicketId);
			setMultiPart(true);
			setMaxSize(Bytes.megabytes(10));

			add(uploadField = new FileUploadField("upload") {
				@Override
				protected void onConfigure() {
					setVisible(status != DataStatus.VALID);
					super.onConfigure();
				}
			});

			confirmationDisplay = new ConfirmationDisplay("confirmationDisplay");
			add(confirmationDisplay);

			add(new FeedbackPanel("feedback"));

			add(new CommitButton("commit-button"));
			add(new CancelButton("cancel-button"));
		}


		@Override
		protected void onSubmit() {
			final FileUpload upload = uploadField.getFileUpload();
			if (upload != null) {
				reader = adminPageService.getUserUpdateSpreadsheetReader();
				boolean success;
				try {
					success = reader.readInput(upload.getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
					success = false;
				}

				if (!success) {
					if (!Strings.isEmpty(reader.getGlobalError()))
						error(reader.getGlobalError());
					else
						error("One or more errors in the file, see below.");
					status = DataStatus.INVALID;
				}

				if(success) {
					info("File looks valid, please review changes then save.");
					status = DataStatus.VALID;
				}

			} else {
				error("Please select a file of updates to upload.");
			}
		}


		private class CommitButton extends Button {
			public CommitButton(String wicketId) {
				super(wicketId);
				setDefaultFormProcessing(false); // no need for validation
			}

			@Override
			protected void onConfigure() {
				setVisible(status== DataStatus.VALID);
				super.onConfigure();
			}

			@Override
			public void onSubmit() {
				reader.save(this);
				status = DataStatus.EMPTY;
				info("Updates Submitted!");
			}

		}
	}

	private class CancelButton extends Button {
		public CancelButton(String wicketId) {
			super(wicketId);
			setDefaultFormProcessing(false); // don't validate on cancel
		}

		@Override
		protected void onConfigure() {
			setVisible(status == DataStatus.VALID);
			super.onConfigure();
		}

		@Override
		public void onSubmit() {
			log.debug("Cancel Button Pressed");
			reader = null;
			status = DataStatus.EMPTY;
		}
	}


	protected class ConfirmationDisplay extends RefreshingView<UserSpreadsheetReader.PotentialUserSave> {

		@Inject
		private IModelProvider modelProvider;

		protected ConfirmationDisplay(String wicketId) {
			super(wicketId);
		}

		@Override
		protected void onConfigure() {
			super.onConfigure();
			List<UserSpreadsheetReader.PotentialUserSave> userList = getPotentialUserList();
			setVisible(userList != null && !userList.isEmpty());
		}

		@Override
		protected Iterator<IModel<UserSpreadsheetReader.PotentialUserSave>> getItemModels() {
			return new ModelIteratorAdapter<UserSpreadsheetReader.PotentialUserSave>(getPotentialUserList()) {
				@Override
				protected IModel<UserSpreadsheetReader.PotentialUserSave> model(UserSpreadsheetReader.PotentialUserSave object) {
					return modelProvider.modelOf(object);
				}
			};
		}

		@Override
		protected void populateItem(Item<UserSpreadsheetReader.PotentialUserSave> item) {
			IModel<UserSpreadsheetReader.PotentialUserSave> mp = item.getModel();
			IModel<User> mu = item.getModelObject().getUser();

			item.add(new Label("line", new PropertyModel<Integer>(mp, "csvRecord.recordNumber")));
			item.add(new Label("username", new PropertyModel<Integer>(mu, "username")));
			item.add(new Label("subjectId", new PropertyModel<Integer>(mu, "subjectId")));
			item.add(new Label("role", new PropertyModel<Integer>(mu, "role")));
			item.add(new Label("periods", new UserPeriodNamesModel(mu)));
			item.add(new Label("fullname", new PropertyModel<Integer>(mu, "fullName")));
			item.add(new Label("email", new PropertyModel<Integer>(mu, "email")));
			item.add(new Label("permission", new PropertyModel<Integer>(mu, "permission")));
			item.add(new Label("error", new PropertyModel<String>(mp, "error")));

		}
	}
}
