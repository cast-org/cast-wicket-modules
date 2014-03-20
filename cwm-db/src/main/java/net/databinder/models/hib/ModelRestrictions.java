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
