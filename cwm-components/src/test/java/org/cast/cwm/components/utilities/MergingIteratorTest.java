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

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class MergingIteratorTest {
	
	Iterator<String> iterator1;
	Iterator<String> iterator2;
	Comparator<String> comparator;
	Iterator<String> mergedIterator;

	@Before
	public void setUp() {
		comparator = String::compareTo;
	}

	@Test
	public void testEmptyMerge() {
		iterator1 = Collections.<String>emptyList().iterator();
		iterator2 = Collections.<String>emptyList().iterator();
		mergedIterator = new MergingIterator<String>(iterator1, iterator2, comparator);
		List<String> mergedList = toList(mergedIterator);
		assertEquals( Collections.<String>emptyList(), mergedList);
	}

	@Test
	public void testSimpleMergeWithEmptyFirst() {
		List<String> listOfOne = Collections.singletonList("a");
		iterator2 = listOfOne.iterator();
		iterator1 = Collections.<String>emptyList().iterator();
		mergedIterator = new MergingIterator<String>(iterator1, iterator2, comparator);
		List<String> mergedList = toList(mergedIterator);
		assertEquals( listOfOne, mergedList);
	}

	@Test
	public void testSimpleMergeWithEmptySecond() {
		List<String> listOfOne = Collections.singletonList("a");
		iterator1 = listOfOne.iterator();
		iterator2 = Collections.<String>emptyList().iterator();
		mergedIterator = new MergingIterator<String>(iterator1, iterator2, comparator);
		List<String> mergedList = toList(mergedIterator);
		assertEquals( listOfOne, mergedList);
	}

	@Test
	public void testSimpleNonEmptyMerge() {
		iterator1 = Collections.singletonList("a").iterator();
		iterator2 = Collections.singletonList("b").iterator();
		mergedIterator = new MergingIterator<String>(iterator1, iterator2, comparator);
		List<String> mergedList = toList(mergedIterator);
		assertEquals( Arrays.asList("a", "b"), mergedList);
	}

	@Test
	public void testSimpleNonEmptyMergeReversed() {
		iterator1 = Collections.singletonList("b").iterator();
		iterator2 = Collections.singletonList("a").iterator();
		mergedIterator = new MergingIterator<String>(iterator1, iterator2, comparator);
		List<String> mergedList = toList(mergedIterator);
		assertEquals( Arrays.asList("a", "b"), mergedList);
	}

	@Test
	public void testLongMerge() {
		iterator1 = Arrays.asList("a", "c", "e").iterator();
		iterator2 = Arrays.asList("b", "d", "f").iterator();
		mergedIterator = new MergingIterator<String>(iterator1, iterator2, comparator);
		List<String> mergedList = toList(mergedIterator);
		assertEquals(Arrays.asList("a", "b", "c", "d", "e", "f"), mergedList);
	}

	private List<String> toList(Iterator<String> iterator) {
		List<String> mergedList = new ArrayList<String>();
		while(iterator.hasNext())
			mergedList.add(mergedIterator.next());
		return mergedList;
	}

}
