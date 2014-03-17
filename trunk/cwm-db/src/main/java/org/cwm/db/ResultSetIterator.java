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
package org.cwm.db;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.hibernate.ScrollableResults;

public class ResultSetIterator<T> implements Iterator<T> {
	
	private ScrollableResults results;
	
	// Our internal pointer is always pointing to what will be returned later by next().
	// This flag is true if there is a valid "next" result?
	private boolean isValid; 

	public ResultSetIterator(Class<T> resultClass, ScrollableResults scrollableResults) {
		this.results = scrollableResults;
		isValid = results.first(); // will set to true if there are any results.
	}

	@Override
	public boolean hasNext() {
		return isValid;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		if (!isValid)
			throw new NoSuchElementException();
		T value = (T) results.get(0);
		isValid = results.next();
		return value;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
