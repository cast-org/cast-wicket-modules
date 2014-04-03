package org.cast.cwm.components.models;

import java.util.Collection;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * A model that will return the size of some collection.
 */
public class CountModel extends AbstractReadOnlyModel<Integer> {

	private IModel<? extends Collection<?>> delegate;

	private static final long serialVersionUID = 1L;
	
	public CountModel(IModel<? extends Collection<?>> delegate) {
		Args.notNull(delegate, "Chained model");
		this.delegate = delegate;
	}
	
	@Override
	public Integer getObject() {
		return delegate.getObject().size();
	}

	@Override
	public void detach() {
		delegate.detach();
	}
	
}
