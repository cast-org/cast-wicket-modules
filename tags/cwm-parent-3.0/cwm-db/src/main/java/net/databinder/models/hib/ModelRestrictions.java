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
package net.databinder.models.hib;

import org.apache.wicket.model.IModel;

/**
 * Factory methods for creating Criteria instances for comparing DB fields to objects wrapped in Models.
 * 
 * @author bgoldowsky
 * 
 */
public class ModelRestrictions {

	private ModelRestrictions() {
		//cannot be instantiated
	}
	
	/**
	 * Apply an "equal" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return ModelExpression
	 */
	public static ModelExpression eq(String propertyName, IModel<?> value) {
		return new ModelExpression(propertyName, value, "=");
	}
	
	/**
	 * Apply a "not equal" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return ModelExpression
	 */
	public static ModelExpression ne(String propertyName, IModel<?> value) {
		return new ModelExpression(propertyName, value, "<>");
	}

	/**
	 * Apply a "like" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */

	public static ModelExpression like(String propertyName, IModel<?> value) {
		return new ModelExpression(propertyName, value, " like ");
	}
	
	/**
	 * Apply a "greater than" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static ModelExpression gt(String propertyName, IModel<?> value) {
		return new ModelExpression(propertyName, value, ">");
	}
	
	/**
	 * Apply a "less than" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static ModelExpression lt(String propertyName, IModel<?> value) {
		return new ModelExpression(propertyName, value, "<");
	}
	
	/**
	 * Apply a "less than or equal" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static ModelExpression le(String propertyName, IModel<?> value) {
		return new ModelExpression(propertyName, value, "<=");
	}
	
	/**
	 * Apply a "greater than or equal" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static ModelExpression ge(String propertyName, IModel<?> value) {
		return new ModelExpression(propertyName, value, ">=");
	}

}
