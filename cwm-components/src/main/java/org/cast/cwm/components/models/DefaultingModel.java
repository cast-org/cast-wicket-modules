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
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * Wrap another model and if that one results in a null (or an empty string), return a default instead.
 *
 * The default can be a simple value, or another model that is evaluated when needed.
 *
 * @author bgoldowsky
 *
 */
public class DefaultingModel<T> extends AbstractReadOnlyModel<T> implements IDetachable {

	private final IModel<T> delegateModel;

	private final IModel<T> defaultModel;

	public DefaultingModel(IModel<T> delegateModel, IModel<T> defaultModel) {
		Args.notNull(delegateModel, "delegate model");
		Args.notNull(defaultModel, "default model");
		this.delegateModel = delegateModel;
		this.defaultModel = defaultModel;
	}

	@Override
	public T getObject() {
		T value = delegateModel.getObject();
		if (value == null || "".equals(value))
			return defaultModel.getObject();
		return value;
	}

	@Override
	public void detach() {
		delegateModel.detach();
		defaultModel.detach();
	}

}
