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
