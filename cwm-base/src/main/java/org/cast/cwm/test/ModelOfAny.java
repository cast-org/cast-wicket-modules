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
package org.cast.cwm.test;

import org.apache.wicket.model.IModel;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.mockito.internal.matchers.InstanceOf;

public class ModelOfAny<T> extends BaseMatcher<IModel<T>> {

    private Class<T> object;

	public ModelOfAny(Class<T> modelArg) {
        object = modelArg;
    }

	@SuppressWarnings("unchecked")
	public boolean matches(Object item) {
		if (!(item instanceof IModel))
			return false;
		Matcher<?> instanceOfMatcher = new InstanceOf(object);
		return instanceOfMatcher.matches(((IModel<T>) item).getObject());
	}

	public void describeTo(Description description) {
		description.appendText("model containing an instance of ");
		description.appendValue(object.getName());
	}
	
	@Factory
	public static <T> Matcher<IModel<T>> modelOfAny(Class<T> operand) {
		return new ModelOfAny<T>(operand);
	}

}
