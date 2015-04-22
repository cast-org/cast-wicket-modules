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
package org.cast.cwm.components.utilities;

import java.util.Iterator;

/**
 * IteratorTransform
 * 
 * Transforms an Iterator by applying a given Transform to each entry.
 * 
 * @author droby
 *
 */
public class IteratorTransform<S, T> implements Transform<Iterator<S> ,Iterator<T>> {

	private Transform<S, T> transform;

	public IteratorTransform(Transform<S, T> transform) {
		this.transform = transform;
	}

	public Iterator<T> apply(final Iterator<S> delegate) {
		return new Iterator<T>() {

			public boolean hasNext() {
				return delegate.hasNext();
			}

			public T next() {
				return transform.apply(delegate.next());
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}};
	}

}
