/*
 * Copyright 2011-2015 CAST, Inc.
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
package org.cast.cwm.components.utilities;

import java.util.Comparator;
import java.util.Iterator;

/**
 * MergingIterator
 * 
 * Merges two Iterators sorted by a Comparator, preserving the sort.
 * 
 * This assumes the two iterators are already sorted with respect to the supplied
 * Comparator.
 * 
 * @author droby
 *
 * TODO: I think there's something like this in Guava, which we might want to use instead.
 */
public class MergingIterator<T> implements Iterator<T> {

	private PeekingIterator<T> iterator1;
	private PeekingIterator<T> iterator2;
	private Comparator<T> comparator;

	public MergingIterator(
			Iterator<T> iterator1,
			Iterator<T> iterator2, 
			Comparator<T> comparator) {
		if ((iterator1 == null) || (iterator2 == null) || (comparator == null))
			throw new IllegalArgumentException("Null argument");
		this.iterator1 = new PeekingIterator<T>(iterator1);
		this.iterator2 = new PeekingIterator<T>(iterator2);
		this.comparator = comparator;
	}

	public boolean hasNext() {
		return iterator1.hasNext() || iterator2.hasNext();
	}

	public T next() {
		if (iterator1.hasNext() && (!iterator2.hasNext() || firstLess()))
			return iterator1.next();
		else return iterator2.next();
	}

	private boolean firstLess() {
		T peek1 = iterator1.peek();
		T peek2 = iterator2.peek();
		return comparator.compare(peek1,  peek2) < 0;
	}

	public void remove() {
		  throw new UnsupportedOperationException();
	}

}
