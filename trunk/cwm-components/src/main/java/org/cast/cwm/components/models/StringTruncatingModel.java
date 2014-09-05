/*
 * Copyright 2011-2014 CAST, Inc.
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
package org.cast.cwm.components.models;

import java.text.BreakIterator;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * Model for potentially long strings that will only show the first N characters.
 * Delegates to another string model for the actual content, then intelligently truncates
 * to a given length, optionally adding "…" or some other string to the end if it is truncated.
 *
 * The truncation attempts to return a string that ends at a word boundary. 
 * However, if this would make the string too short (less than half of maxLength), 
 * it will just go ahead and truncate at the last possible character.
 * 
 * Whitespace in the string is normalized in any case, to avoid over-truncation of strings that have 
 * a lot of spaces in them.
 *
 * @author bgoldowsky
 *
 */
public class StringTruncatingModel extends AbstractReadOnlyModel<String> implements IDetachable {

	private IModel<String> delegateModel;

	private int maxLength;
	
	private boolean breakAtWordBoundary = true;

	private String continuationIndicator;

	private static final long serialVersionUID = 1L;

	/**
	 * Construct with all configurable parameters specified.
	 * Continuation indicator can be empty, but not null.
	 * 
	 * @param delegateModel
	 * @param maxLength the maximum length that the output is allowed to be (including the continuation indicator, if present)
	 * @param continuationIndicator  string that will be added to the end to indicate that the value is truncated.
	 */
	public StringTruncatingModel (IModel<String> delegateModel, int maxLength, String continuationIndicator) {
		Args.notNull(delegateModel, "delegate model");
		Args.notNull(continuationIndicator, "continuation indicator");
		this.delegateModel = delegateModel;
		this.maxLength = maxLength;
		this.continuationIndicator = continuationIndicator;
	}
	
	/**
	 * Construct with the default continuation indicator "…"  (unicode ellipses char).
	 * @param delegateModel
	 * @param delegateModel
	 * @param maxLength the maximum length that the output is allowed to be (including the continuation indicator, if present)
	 */
	public StringTruncatingModel (IModel<String> delegateModel, int maxLength) {
		this(delegateModel, maxLength, "…");
	}

	@Override
	public String getObject() {
		String full = delegateModel.getObject();
		
		// Short circuit if string is null or already short.
		if (full == null)
			return null;
		if (full.length() < maxLength)
			return full;
		String normalized = full.trim().replaceAll("\\s{2,}", " ");
		
		int lengthAvailable = maxLength-continuationIndicator.length();
		if (breakAtWordBoundary)
			return truncateToWord(normalized, lengthAvailable, maxLength/2) + continuationIndicator;
		else
			return normalized.substring(0, lengthAvailable) + continuationIndicator;
	}

	// Return the substring of the given string up to the last word boundary 
	// at or before the given position in the string.  If that would be shorter
	// than minLength, returns the string up to pos instead.
	protected String truncateToWord(String orig, int pos, int minLength) {
		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(orig);
		int lengthToLastWord = boundary.preceding(pos);
		if (lengthToLastWord < minLength)
			// would be too short if truncated to word boundary
			return orig.substring(0, pos);
		else
			return orig.substring(0, lengthToLastWord);
	}

	@Override
	public void detach() {
		delegateModel.detach();
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	
	public boolean isBreakAtWordBoundary() {
		return breakAtWordBoundary;
	}

	public void setBreakAtWordBoundary(boolean breakAtWordBoundary) {
		this.breakAtWordBoundary = breakAtWordBoundary;
	}

	public String getContinuationIndicator() {
		return continuationIndicator;
	}

	public void setContinuationIndicator(String continuationIndicator) {
		this.continuationIndicator = continuationIndicator;
	}
	
}
