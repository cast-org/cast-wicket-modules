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
package org.cast.cwm.data.component;

import java.util.List;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.Period;

/** A DropDownChoice of Periods.
 * 
 * @author bgoldowsky
 *
 */
public class PeriodChoice extends DropDownChoice<Period> {

	private static final long serialVersionUID = 1L;
	
	private static ChoiceRenderer<Period> renderer = new ChoiceRenderer<Period>("name", "id");

	/**
	 * Normal constructor, uses the list of Periods associated with the currently logged-in user.
	 * @param wicketId
	 * @param model
	 */
	public PeriodChoice (String wicketId, IModel<Period> model) {
		this (wicketId, model, new PropertyModel<List<Period>>(CwmSession.get().getUserModel(), "periodsAsList"));
	}
	
	/**
	 * Construct with a model for the list of Periods from which to choose.
	 * @param id
	 * @param model
	 * @param choices
	 */
	public PeriodChoice(String id, IModel<Period> model, IModel<? extends List<? extends Period>> choices) {
		super(id, model, choices, renderer);
	}

}
