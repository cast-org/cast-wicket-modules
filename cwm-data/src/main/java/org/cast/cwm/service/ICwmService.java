package org.cast.cwm.service;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.init.IDatabaseInitializer;

public interface ICwmService {

	/**
	 * Checks the model wrapping a {@link PersistedObject} to confirm that
	 * it has properly been implemented to work with the underlying datastore.
	 * 
	 * Override this method to provide your own custom implementation.
	 * @param objectModel
	 */
	void confirmDatastoreModel(IModel<? extends PersistedObject> objectModel);

	/**
	 * Look up a datastore object by its ID.  This method is implemented using
	 * the underlying datastore system.
	 * 
	 */
	<T extends PersistedObject> IModel<T> getById(Class<T> clazz, long id);

	/**
	 * Add a {@link PersistedObject} to the datastore.
	 * Does not take a model as parameter because you usually won't have one for a
	 * brand new object.
	 * @param object
	 */
	void save(PersistedObject object);

	/**
	 * Delete a {@link PersistedObject} from the datastore.
	 * 
	 * @param objectModel
	 */
	void delete(IModel<? extends PersistedObject> objectModel);

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
	 * @param catchErrors
	 */
	void flushChanges(boolean catchErrors);

	/**
	 * Return a list of all distinct initializers that have been run.
	 * @return list of names of initializers that have at some point been run.
	 */
	@SuppressWarnings("unchecked")
	List<String> getInitializationNames();

	/**
	 * Record an initialization record in the database.
	 * @param initializer the database initializer that was run.
	 */
	void saveInitialization(IDatabaseInitializer izer);

}