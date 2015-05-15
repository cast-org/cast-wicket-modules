/*
 * Copyright 2011-2015 CAST, Inc.
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
package org.cwm.db.service;

import java.io.Serializable;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.model.IModel;

public class HibernateObjectModelProvider implements IModelProvider {

	public <T  extends Serializable> IModel<T> modelOf(T object) {
		return new HibernateObjectModel<T>(object);
	}

	public <T extends Serializable> IModel<T> emptyModel(Class<T> clazz) {
		return new HibernateObjectModel<T>(clazz, 0L);
	}

}
