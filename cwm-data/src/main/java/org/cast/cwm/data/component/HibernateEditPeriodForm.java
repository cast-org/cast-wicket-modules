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
package org.cast.cwm.data.component;

import com.google.inject.Inject;
import net.databinder.components.hib.DataForm;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.validator.UniqueDataFieldValidator;
import org.cast.cwm.service.ISiteService;

public class HibernateEditPeriodForm<T extends Period> extends DataForm<T>{

	@Inject
	private ISiteService siteService;

	private IModel<Site> mSite;

	public HibernateEditPeriodForm(String id, HibernateObjectModel<T> model) {
		super(id, model);
		mSite = new PropertyModel<Site>(model, "site");
		addFields();
	}
	
	public HibernateEditPeriodForm(String id, IModel<Site> siteModel, Class<T> periodClass) {
		super(id, periodClass);
		mSite = siteModel;
		addFields();
	}

	private void addFields() {
		add(new FeedbackPanel("feedback"));

		// Period name
		RequiredTextField<String> name = new RequiredTextField<String>("name");
		name.add(StringValidator.lengthBetween(1, 32));
		name.add(new PatternValidator("[\\w!@#$%^&*()=_+;:/ -]+")); // NO comma since spreadsheet upload uses comma-sep list.

		UniqueDataFieldValidator<String> nameValidator;
		if (getModelObject().isTransient())
			nameValidator = new UniqueDataFieldValidator<String>(Period.class, "name");
		else
			nameValidator = new UniqueDataFieldValidator<String>(getModel(), "name");
		nameValidator.limitScope("site", mSite);
		name.add(nameValidator);

		add(new FeedbackBorder("nameBorder").add(name));

		// Anonymous ClassId
		RequiredTextField<String> classId = new RequiredTextField<String>("classId");
		classId.add(StringValidator.lengthBetween(1, 32));
		classId.add(new PatternValidator("[\\w!@#$%^&*()=_+;:/ -]+"));

		UniqueDataFieldValidator<String> idValidator;
		if (getModelObject().isTransient())
			idValidator = new UniqueDataFieldValidator<String>(Period.class, "classId");
		else
			idValidator = new UniqueDataFieldValidator<String>(getModel(), "classId");
		classId.add(idValidator);

		add(new FeedbackBorder("classIdBorder").add(classId));
	}
	
	@Override
	protected void onSubmit() {
		boolean periodIsNew = getModelObject().isTransient();
		String message = periodIsNew ? "Saved." : "Updated.";
		getModelObject().setSite(mSite.getObject());
		super.onSubmit();
		info("Period '" + getModelObject().getName() + "' " + message);

		if (periodIsNew)
			onPeriodCreated(getModel());
		else
			onPeriodEdited(getModel());
	}

	protected void onPeriodCreated(IModel<T> mPeriod) {
		siteService.onPeriodCreated(mPeriod);

	}

	private void onPeriodEdited(IModel<T> mPeriod) {
		siteService.onPeriodEdited(mPeriod);
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		mSite.detach();
	}
}

