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

public class HibernateEditPeriodForm<T extends Period> extends DataForm<T>{

	private static final long serialVersionUID = 1L;
	private IModel<Site> site;
	
	public HibernateEditPeriodForm(String id, HibernateObjectModel<T> model) {
		super(id, model);
		site = new PropertyModel<Site>(model, "site");
		addFields();
	}
	
	public HibernateEditPeriodForm(String id, IModel<Site> siteModel, Class<T> periodClass) {
		super(id, periodClass);
		getModelObject().setSite(siteModel.getObject());
		site = siteModel;
		addFields();
	}

	private void addFields() {

		add(new FeedbackPanel("feedback"));
		UniqueDataFieldValidator<String> validator;
		if (getModelObject().isTransient())
			validator = new UniqueDataFieldValidator<String>(Period.class, "name");
		else
			validator = new UniqueDataFieldValidator<String>(getModel(), "name");
		validator.limitScope("site", site);
		
		add(new FeedbackBorder("nameBorder").add(
				new RequiredTextField<String>("name")
				.add(StringValidator.lengthBetween(1, 32))
				.add(new PatternValidator("[\\w!@#$%^&*()=_+;:/ -]+")) // NO comma since spreadsheet upload uses comma-sep list.
				.add(validator)));
	}
	
	@Override
	protected void onSubmit() {
		String message = getModelObject().isTransient() ? "Saved." : "Updated.";
		super.onSubmit();
		info("Period '" + getModelObject().getName() + "' " + message);
	}
	
	@Override 
	protected void onDetach() {
		site.detach();
		super.onDetach();
	}
}

