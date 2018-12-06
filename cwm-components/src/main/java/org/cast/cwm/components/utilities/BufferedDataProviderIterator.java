/*
 * Copyright 2011-2019 CAST, Inc.
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

import org.apache.wicket.markup.repeater.data.IDataProvider;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * BufferedDataProviderIterator<T>
 * 
 * An iterator that iterates over all the data returned from an 
 * implementation of IDataProvider, in chunks of a provided size parameter.
 * 
 * @author droby
 *
 * @param <T> Type of entities in the IDataProvider and this Iterator
 * 
 *  @see org.apache.wicket.markup.repeater.data.IDataProvider, java.util.Iterator
 */
public class BufferedDataProviderIterator<T> implements Iterator<T> {

	private IDataProvider<T> dataProvider;
	private int pageSize;
	private int currentPage;
	private Iterator<? extends T> currentIterator;

	/**
	 * Constructor
	 * 
	 * @param dataProvider - the IDataProvider<T> providing the data
	 * @param pageSize - the number of data elements to get at a time.
	 */
	public BufferedDataProviderIterator(IDataProvider<T> dataProvider, int pageSize) {
		this.dataProvider = dataProvider;
		this.pageSize = pageSize;
		this.currentPage = 0;
		this.currentIterator = null;
	}

	public boolean hasNext() {
		ensureIteratorLoaded();
		boolean nextInCurrentIterator = currentIterator.hasNext();
		if (nextInCurrentIterator)
			return true;
		int nextPage = (currentPage + 1) * pageSize;
		if (nextPage >= dataProvider.size())
			return false;
		currentPage++;
		currentIterator = dataProvider.iterator(currentPage*pageSize, pageSize);
		return currentIterator.hasNext();
	}

	public T next() {
		if (hasNext())
			return currentIterator.next();
		else
			throw new NoSuchElementException();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	private void ensureIteratorLoaded() {
		if (currentIterator == null)
			currentIterator = dataProvider.iterator(0, pageSize);
	}

}
