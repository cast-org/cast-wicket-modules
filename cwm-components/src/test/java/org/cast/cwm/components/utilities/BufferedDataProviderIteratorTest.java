/*
 * Copyright 2011-2020 CAST, Inc.
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;

public class BufferedDataProviderIteratorTest {

	private IDataProvider<String> dataProvider;
	private Iterator<String> iterator;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		dataProvider = mock(IDataProvider.class);
		iterator = new BufferedDataProviderIterator<String>(dataProvider, 5);
	}
	
	@Test
	public void initialHasNextGetsIteratorFromDataProvider() {
		@SuppressWarnings("unchecked")
		final Iterator<? extends String> firstIterator = mock(Iterator.class);
		stubDataProvider(firstIterator, 0, 5);
		when(firstIterator.hasNext()).thenReturn(true);
		iterator.hasNext();
		verify(dataProvider).iterator(0, 5);
	}

	@Test
	public void initiallyHasNextDelegatesToFirstIterator() {
		@SuppressWarnings("unchecked")
		final Iterator<? extends String> firstIterator = mock(Iterator.class);
		stubDataProvider(firstIterator, 0, 5);
		when(firstIterator.hasNext()).thenReturn(true);
		iterator.hasNext();
		verify(firstIterator).hasNext();
	}

	@Test
	public void firstNextGetsIteratorFromDataProviderIfNeeded() {
		@SuppressWarnings("unchecked")
		final Iterator<? extends String> firstIterator = mock(Iterator.class);
		stubDataProvider(firstIterator, 0, 5);
		when(firstIterator.hasNext()).thenReturn(true);
		iterator.next();
		verify(dataProvider).iterator(0, 5);
	}

	@Test
	public void firstNextDelegatesToFirstIterator() {
		@SuppressWarnings("unchecked")
		final Iterator<? extends String> firstIterator = mock(Iterator.class);
		stubDataProvider(firstIterator, 0, 5);
		when(firstIterator.hasNext()).thenReturn(true);
		iterator.next();
		verify(firstIterator).next();
	}

	@Test
	public void nextChecksForHasNext() {
		@SuppressWarnings("unchecked")
		final Iterator<? extends String> firstIterator = mock(Iterator.class);
		stubDataProvider(firstIterator, 0, 5);
		when(firstIterator.hasNext()).thenReturn(true);
		iterator.next();
		verify(firstIterator).hasNext();
	}

	@SuppressWarnings("unchecked")
	@Test(expected=NoSuchElementException.class)
	public void nextThrowsExceptionWhenNoMore() {
		final Iterator<? extends String> firstIterator = mock(Iterator.class);
		final Iterator<? extends String> secondIterator = mock(Iterator.class);
		stubDataProvider(firstIterator, 0, 5);
		stubDataProvider(secondIterator, 5, 5);
		when(firstIterator.hasNext()).thenReturn(true, false);
		when(secondIterator.hasNext()).thenReturn(false);
		iterator.next();
		iterator.next();
		verify(firstIterator, times(2)).hasNext();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getsSecondIteratorWhenNeeded() {
		final Iterator<? extends String> firstIterator = mock(Iterator.class);
		final Iterator<? extends String> secondIterator = mock(Iterator.class);
		stubDataProvider(firstIterator, 0, 5);
		stubDataProvider(secondIterator, 5, 5);
		when(dataProvider.size()).thenReturn(8L);
		when(firstIterator.hasNext()).thenReturn(true, true, true, true, true, false);
		when(secondIterator.hasNext()).thenReturn(true);
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		verify(firstIterator, times(6)).hasNext();
		verify(firstIterator, times(5)).next();
		verify(secondIterator, times(1)).hasNext();
		verify(secondIterator, times(1)).next();
	}

	@SuppressWarnings("unchecked")
	@Test(expected=NoSuchElementException.class)
	public void stopsAtDataProviderSize() {
		final Iterator<? extends String> firstIterator = mock(Iterator.class);
		stubDataProvider(firstIterator, 0, 5);
		when(dataProvider.size()).thenReturn(5L);
		when(firstIterator.hasNext()).thenReturn(true, true, true, true, true, false);
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		verify(firstIterator, times(6)).hasNext();
		verify(firstIterator, times(5)).next();
	}

	private void stubDataProvider(final Iterator<? extends String> firstIterator, int start, int length) {
		when(dataProvider.iterator(start, length)).then(new Answer<Iterator<? extends String>>(){

			public Iterator<? extends String> answer(InvocationOnMock invocation)
					throws Throwable {
				return firstIterator;
			}});
	}
}
