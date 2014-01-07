package org.cwm.db.service;

import java.io.Serializable;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class SimpleModelProvider implements IModelProvider {

	public <T extends Serializable> IModel<T> modelOf(T object) {
		return Model.of(object);
	}

	public <T extends Serializable> IModel<T> emptyModel(Class<T> clazz) {
		return new Model<T>();
	}

}
