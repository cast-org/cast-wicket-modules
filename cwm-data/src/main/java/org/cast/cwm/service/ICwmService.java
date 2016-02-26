/*
 * Copyright 2011-2016 CAST, Inc.
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
package org.cast.cwm.service;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.init.IDatabaseInitializer;

import java.util.List;

public interface ICwmService {

	/**
	 * Checks the model wrapping a {@link PersistedObject} to confirm that
	 * it has properly been implemented to work with the underlying datastore.
	 * 
	 * @param objectModel model to check
	 */
	void confirmDatastoreModel(IModel<? extends PersistedObject> objectModel);

	/**
	 * Look up a datastore object by its ID.  This method is implemented using
	 * the underlying datastore system.
	 *
	 * @param clazz type of the object (subclass of PersistedObject)
	 * @param id database ID of the object
	 */
	<T extends PersistedObject> IModel<T> getById(Class<T> clazz, long id);

	/**
	 * Add a {@link PersistedObject} to the datastore.
	 * Does not take a model as parameter because you usually won't have one for a
	 * brand new object.
	 * @param object object to make persistent
	 */
	void save(PersistedObject object);

	/**
	 * Delete a {@link PersistedObject} from the datastore.
	 * 
	 * @param objectModel model of object to delete
	 */
	void delete(IModel<? extends PersistedObject> objectModel);
	
	/**
	 * Delete a {@link PersistedObject} from the datastore.
	 * 
	 * @param object to delete
	 */
	void delete(PersistedObject object);

	/**
	 * Flush changes to the datastore.  Essentially, this commits the previous
	 * transaction and starts a new transaction.  This should be run at the end of
	 * any Service method that is making changes to the datastore.
	 */
	void flushChanges();

	/**
	 * <p>
	 * Flush changes to the datastore.  Essentially, this commits the previous
	 * transaction and starts a new transaction.  This should be run at the end of
	 * any Service method that is making changes to the datastore.
	 * </p>
	 * 
	 * <p>
	 * If catchErrors is true, the commit will be run in a <em>try</em> block
	 * and any exceptions will be ignored.
	 * </p>
	 * 
	 * @param catchErrors true to ignore exceptions
	 */
	void flushChanges(boolean catchErrors);

	/**
	 * Return a list of all distinct initializers that have been run.
	 * @return list of names of initializers that have at some point been run.
	 */
	List<String> getInitializationNames();

	/**
	 * Record an initialization record in the database.
	 * @param initializer the database initializer that was run.
	 */
	void saveInitialization(IDatabaseInitializer initializer);

}