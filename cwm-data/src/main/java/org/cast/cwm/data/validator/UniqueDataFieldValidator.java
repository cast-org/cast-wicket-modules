/*
 * Copyright 2011-2019 CAST, Inc.
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
package org.cast.cwm.data.validator;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.databinder.models.hib.ModelRestrictions;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.cwm.db.service.IDBService;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A validator for confirming field uniqueness for objects in the datastore.  For example:
 * <p>
 * <code>
 * new TextField<String>("username").add(new UniqeDataFieldValidator<String>(User.class, "username"))
 * </code>
 * </p>
 * will ensure that the username chosen in the text field is not already in use by another {@link User}.
 * If you are editing an existing user, you must indicate that user using the alternate 
 * constructor {@link #UniqueDataFieldValidator(IModel, String)}.  Otherwise, the validator will fail
 * because the value is in use by that user and will therefore not be unique if created again.
 * <br />
 * 
 * @author jbrookover
 *
 * @param <T>
 */
public class UniqueDataFieldValidator<T> implements IValidator<T>, IDetachable {

	private Class<? extends PersistedObject> clazz;
	private IModel<? extends PersistedObject> mCurrent = null;
	private String field;
	private Map<String, IModel<? extends Serializable>> scopes = new HashMap<>();

	@Getter @Setter
	private boolean caseSensitive = true;

	@Inject
    private IDBService dbService;

	/**
	 * Constructor used when editing a persistent object.
	 *
	 * @param mCurrent model of the object being edited
	 * @param fieldName the field to confirm for uniqueness among objects of this type
	 */
	public UniqueDataFieldValidator(IModel<? extends PersistedObject> mCurrent, String fieldName) {
		this(mCurrent.getObject().getClass(), fieldName);
	    this.mCurrent = mCurrent;
	}
	
	/**
	 * Constructor used when creating a new persistent object.
	 *
	 * @param clazz the type of object to check
	 * @param fieldName the field to confirm for uniqueness among objects of this type
	 */
	public UniqueDataFieldValidator(Class<? extends PersistedObject> clazz, String fieldName) {
		this.clazz = clazz;
		this.field = fieldName;
        Injector.get().inject(this);
	}

	@Override
	public void validate(final IValidatable<T> validatable) {
        Criteria criteria = dbService.getHibernateSession().createCriteria(clazz);

        SimpleExpression comparison = Restrictions.eq(field, validatable.getValue());
        if (!isCaseSensitive())
            comparison.ignoreCase();
        criteria.add(comparison);

        for (String field : scopes.keySet()) {
            criteria.add(ModelRestrictions.eq(field, scopes.get(field)));
        }

        // Normally, there will be at most one object found, but make this robust to other cases.
        // If any object found is not the current object, raise an error.
        for (Object found : criteria.list()) {
            if (mCurrent != null && mCurrent.getObject().equals(found)) {
                // Finding current object is expected; ignore
                continue;
            }
            ValidationError err = new ValidationError(this).addKey(getResourceKey());
            err.setVariables(getVariablesMap());
            validatable.error(err);
            return;
        }
	}

	/**
	 * Limit the scopes of this validation.  Allows you to ensure that a
	 * data field is unique only within a certain group.  For example,
	 * <code>
	 * <pre>
	 * Site x = ...;
	 * validator.limitScope('site', siteModel);
	 * </pre>
	 * </code> 
	 * would limit the unique field validation to a given site, allowing
	 * you to have objects with non-unique fields in <i>different</i> sites.
	 *
	 * This method can be called multiple times to add several scope limits,
	 * all of which must be satisfied.
	 * 
	 * @param field the name of the field in the object
	 * @param value the object that this field must represent
	 * @return this object, for chaining
	 */
	public UniqueDataFieldValidator<T> limitScope(String field, IModel<? extends Serializable> value) {
		scopes.put(field, value);
		return this;
	}

	private String getResourceKey() {
		return Classes.simpleName(getClass());
	}

	protected Map<String, Object> getVariablesMap() {
		Map<String, Object> map = new HashMap<>(2);
		map.put("field", field);
		map.put("object", clazz.getSimpleName());
		return map;
	}

	@Override
	public void detach() {
		if (mCurrent != null)
			mCurrent.detach();
		for (IModel<?> model : scopes.values())
		    model.detach();
	}

}
