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
package org.cast.cwm.components.models;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * A model for a word or phrase that has to be singular or plural depending on some value.
 * This takes a delegate model for the number, and two strings for the singular and plural versions.
 * 
 * The number itself is not part of the result of this Model.
 * If you need that, use @SingularPluralCountModel.
 */
public class SingularPluralModel extends AbstractReadOnlyModel<String> {

	protected IModel<? extends Number> mNumber;

	protected String singular;

	protected String plural;

	private static final long serialVersionUID = 1L;

	/**
	 * Construct a model with a given value field, singular form, and plural form.
	 * @param mNumber delegate model whose object is a number
	 * @param singular string to return when number is 1
	 * @param plural string to return when number is not 1
	 */
	public SingularPluralModel(IModel<? extends Number> mNumber, String singular, String plural) {
		Args.notNull(mNumber, "Chained model");
		Args.notNull(singular, "Singular form");
		this.mNumber = mNumber;
		this.singular = singular;
		this.plural = plural;
	}
	
	/**
	 * Construct for regular English pluralization.
	 * The plural form will be assumed to be the singular form with an "s" added. 
	 * @param mNumber delegate model whose object is a number
	 * @param singular string to return when number is 1
	 */
	public SingularPluralModel(IModel<? extends Number> mNumber, String singular) {
		this(mNumber, singular, singular+"s");
	}
	
	@Override
	public String getObject() {
		if (mNumber.getObject() == null)
			return null;
		if (mNumber.getObject().intValue() == 1)
			return singular;
		else
			return plural;
	}

	@Override
	public void detach() {
		mNumber.detach();
	}
	
}
