/*
 * Copyright 2011-2018 CAST, Inc.
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
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.service.ICwmSessionService;

import java.util.List;

/** A DropDownChoice of Periods.
 * 
 * @author bgoldowsky
 *
 */
public class PeriodChoice extends DropDownChoice<Period> {

	@Inject
	private ICwmSessionService cwmSessionService;
	
	private static ChoiceRenderer<Period> renderer = new ChoiceRenderer<>("name", "id");

	/**
	 * Normal constructor; uses the list of Periods associated with the currently logged-in user.
	 * @param wicketId component ID
	 * @param mPeriod model for the choice
	 */
	public PeriodChoice (String wicketId, IModel<Period> mPeriod) {
		this (wicketId, mPeriod, null);
		setChoices(new PropertyModel<List<Period>>(cwmSessionService.getUserModel(), "periodsAsList"));
	}
	
	/**
	 * Construct with a model for the list of Periods from which to choose.
	 * @param wicketId component ID
	 * @param mPeriod model for the choice
	 * @param mChoices model that resolves to the list of choices to be presented in the menu.
	 */
	public PeriodChoice(String wicketId, IModel<Period> mPeriod, IModel<? extends List<? extends Period>> mChoices) {
		super(wicketId, mPeriod, mChoices, renderer);
	}

}
