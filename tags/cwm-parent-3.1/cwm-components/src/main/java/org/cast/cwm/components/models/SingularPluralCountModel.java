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
package org.cast.cwm.components.models;

import org.apache.wicket.model.IModel;

/**
 * A model for a phrase with a count followed by a word or words that have to be singular or plural.
 * This takes a delegate model for the number, and two strings for the singular and plural versions.
 * 
 * The result will be the count followed by a space and then the words, but this can be adjusted by
 * overriding the {@link #format(Number, String)} method.
 * 
 * If the number itself should not be part of the result, use @SingularPluralModel instead.
 */
public class SingularPluralCountModel extends SingularPluralModel {

	private static final long serialVersionUID = 1L;

	public SingularPluralCountModel(IModel<? extends Number> mNumber, String singular, String plural) {
		super(mNumber, singular, plural);
	}
	public SingularPluralCountModel(IModel<? extends Number> mNumber, String singular) {
		super(mNumber, singular);
	}

	@Override
	public String getObject() {
		if (mNumber.getObject().equals(1))
			return format(mNumber.getObject(), singular);
		else
			return format(mNumber.getObject(), plural);
	}
	
	protected String format(Number number, String words) {
		return String.format("%d %s", number, words);
	}

}
