package net.databinder.models.hib;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.engine.spi.TypedValue;

/**
 * Criteria object for comparing a database field to an object wrapped in an IModel.
 * This is a variant of Hibernate's SimpleExpression.
 *  
 * @author bgoldowsky
 *
 */
public class ModelExpression extends SimpleExpression implements IDetachable {
	
	protected final boolean ignoreCase;
	protected IModel<?> model;

	private static final long serialVersionUID = 1L;

	protected ModelExpression(String propertyName, IModel<?> model, String op) {
		this(propertyName, model, op, false);
	}

	public ModelExpression(String propertyName, IModel<?> model, String op, boolean ignoreCase) {
		super(propertyName, null, op, ignoreCase);
		this.model = model;
		this.ignoreCase = ignoreCase;
	}

	@Override
	public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) 
			throws HibernateException {
		Object value = getValue();
		Object icvalue = ignoreCase ? value.toString().toLowerCase() : value;
		return new TypedValue[] {criteriaQuery.getTypedValue( criteria, getPropertyName(), icvalue )};
	}

	@Override
	public String toString() {
		return getPropertyName() + getOp() + model.toString();
	}

	@Override
	public Object getValue() {
		return model.getObject();
	}

	@Override
	public void detach() {
		model.detach();
	}

}
