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
package org.cast.cwm.service;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.cast.cwm.data.init.IDatabaseInitializer;
import org.cast.cwm.db.data.PersistedObject;

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
	 * Return a list of all distinct initializers that have been run.
	 * @return list of names of initializers that have at some point been run.
	 */
	List<String> getInitializationNames();

	/**
	 * Record an initialization record in the database.
	 * @param initializer the database initializer that was run.
	 */
	void saveInitialization(IDatabaseInitializer initializer);

	/**
	 * Return a ResourceReference for the Loglevel Javascript library.
	 * @return reference
	 */
    JavaScriptResourceReference getLoglevelJavascriptResourceReference();

}