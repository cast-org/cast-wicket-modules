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
package org.cast.cwm.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;
import org.apache.wicket.behavior.AttributeAppender;

/**
 * The opposite of {@link AttributeAppender}. Adds the given value 
 * to the beginning instead of the end of an attribute.  This behavior
 * will create the attribute if it is not present.
 */
public class AttributePrepender extends AttributeModifier {

	private static final long serialVersionUID = 1L;
	private String separator;

	/**
	 * Constructor
	 * 
	 * @param attribute - the attribute to modify
	 * @param value - the value to add
	 * @param separator - the separator to insert between the new value and the existing value
	 */
	public AttributePrepender(String attribute, String value, String separator) {
		super(attribute, true, Model.of(value));
		this.separator = separator;
	}
	@Override
	protected String newValue(final String currentValue, final String replacementValue) {
		if(replacementValue == null || replacementValue.equals(""))
			return currentValue;
		if(currentValue == null || currentValue.equals(""))
			return replacementValue;
		return replacementValue + separator + currentValue;
	}
	
}