/*
 * Copyright 2011-2020 CAST, Inc.
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
