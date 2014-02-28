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

import java.util.List;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;

/** A DropDownChoice of Users.
 * 
 * @author bgoldowsky
 *
 */
public class UserChoice extends DropDownChoice<User> {

	private static final long serialVersionUID = 1L;
	
	private static ChoiceRenderer<User> renderer = new ChoiceRenderer<User>("sortName", "id");

	/**
	 * Construct with a model for the list of Users from which to choose.
	 * @param id
	 * @param model
	 * @param choices
	 */
	public UserChoice(String id, IModel<User> model, IModel<? extends List<? extends User>> choices) {
		super(id, model, choices, renderer);
	}

}
