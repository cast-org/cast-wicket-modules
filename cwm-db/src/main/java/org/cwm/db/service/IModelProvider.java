package org.cwm.db.service;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

public interface IModelProvider {

	<T extends Serializable> IModel<T> modelOf(T object);
	<T extends Serializable> IModel<T> emptyModel(Class<T> clazz);

}
