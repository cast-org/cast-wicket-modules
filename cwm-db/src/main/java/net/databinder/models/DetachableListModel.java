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
package net.databinder.models;

import org.apache.wicket.model.IModel;
import org.cwm.db.service.IModelProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model that can hold, and correctly detach, a list of detachable items.
 *
 * @author bgoldowsky
 */
public class DetachableListModel<T extends Serializable> extends LoadableWritableModel<List<T>> {

	private final IModelProvider modelProvider;

	private List<IModel<T>> modelList = new ArrayList<IModel<T>>();

	public DetachableListModel (IModelProvider modelProvider) {
		this.modelProvider = modelProvider;
	}

	public DetachableListModel (IModelProvider modelProvider, List<T> list) {
		this(modelProvider);
		setObject(list);
	}

	@Override
	protected List<T> load() {
		ArrayList<T> list = new ArrayList<>(modelList.size());
		for (IModel<T> m : modelList)
			list.add(m.getObject());
		return list;
	}

	@Override
	public void setObject(List<T> list) {
		modelList.clear();
		for (T obj : list)
			modelList.add(modelProvider.modelOf(obj));
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		for (IModel<T> m : modelList)
			m.detach();
	}

}
