package org.cast.cwm.components.models;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * Simple model that delegates to another string Model and adds text before and/or after the value of that other model.
 * Useful, for example, if you want a property of some object but with a bit of additional text added.
 * 
 * Example: put an object's name in brackets:
 * <code><pre>
 *   new AppendingModel(new PropertyModel<String>(object, "name"), "[", "]");
 * </pre></code>
 * 
 * @author bgoldowsky
 *
 */
public class StringWrappingModel extends AbstractReadOnlyModel<String> implements IDetachable {

	private IModel<String> delegateModel;
	
	private final String before;
	private final String after;

	private static final long serialVersionUID = 1L;

	/**
	 * Construct with before and after strings.
	 * Before and after can be empty, but not null.
	 * 
	 * @param delegateModel
	 * @param before string to prepend to delegate model's object
	 * @param after  string to append to delegate model's object
	 */
	public StringWrappingModel (IModel<String> delegateModel, String before, String after) {
		Args.notNull(delegateModel, "delegate model");
		Args.notNull(before, "before string");
		Args.notNull(after, "after string");
		this.delegateModel = delegateModel;
		this.before = before;
		this.after = after;
	}

	@Override
	public String getObject() {
		return before + delegateModel.getObject() + after;
	}

	@Override
	public void detach() {
		delegateModel.detach();
	}
	
}
