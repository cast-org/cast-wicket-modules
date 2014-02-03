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
