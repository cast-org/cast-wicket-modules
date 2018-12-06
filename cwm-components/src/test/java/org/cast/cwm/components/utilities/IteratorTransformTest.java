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

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IteratorTransformTest {

	@Test
	public void testMap() {
		List<Integer> list = Arrays.asList(1, 2, 3);
		IteratorTransform<Integer, Integer> mapper = new IteratorTransform<Integer, Integer>(new Transform<Integer, Integer>(){
			public Integer apply(Integer in) {
				return in * 2;
			}});
		assertIteratorsEqual(Arrays.asList(2, 4, 6).iterator(), mapper.apply(list.iterator()));
	}

	private void assertIteratorsEqual(Iterator<Integer> expected,
			Iterator<Integer> actual) {
		assertEquals(getSize(expected), getSize(actual));
		while (actual.hasNext()) {
			assertTrue(expected.hasNext());
			assertEquals(expected.next(), actual.next());
		}
	}

	private Integer getSize(Iterator<Integer> it) {
		int size = 0;
		while (it.hasNext()) {
			size++;
			it.next();
		}
		return size;
	}

}