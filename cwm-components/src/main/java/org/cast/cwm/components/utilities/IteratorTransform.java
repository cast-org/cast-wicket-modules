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
