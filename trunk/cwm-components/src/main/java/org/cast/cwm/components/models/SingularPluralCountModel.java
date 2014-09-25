package org.cast.cwm.components.models;

import org.apache.wicket.model.IModel;

/**
 * A model for a phrase with a count followed by a word or words that have to be singular or plural.
 * This takes a delegate model for the number, and two strings for the singular and plural versions.
 * 
 * The result will be the count followed by a space and then the words, but this can be adjusted by
 * overriding the {@link #format(Number, String)} method.
 * 
 * If the number itself should not be part of the result, use @SingularPluralModel instead.
 */
public class SingularPluralCountModel extends SingularPluralModel {

	private static final long serialVersionUID = 1L;

	public SingularPluralCountModel(IModel<? extends Number> mNumber, String singular, String plural) {
		super(mNumber, singular, plural);
	}
	public SingularPluralCountModel(IModel<? extends Number> mNumber, String singular) {
		super(mNumber, singular);
	}

	@Override
	public String getObject() {
		if (mNumber.getObject().equals(1))
			return format(mNumber.getObject(), singular);
		else
			return format(mNumber.getObject(), plural);
	}
	
	protected String format(Number number, String words) {
		return String.format("%d %s", number, words);
	}

}
