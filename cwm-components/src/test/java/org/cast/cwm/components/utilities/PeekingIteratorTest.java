/*
 * Copyright 2011-2017 CAST, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

public class PeekingIteratorTest {

	private Iterator<String> baseIterator;
	private PeekingIterator<String> peekingIterator;

	@Before
	public void setUp() {
		baseIterator = Arrays.asList("a", "b", "c").iterator();
		peekingIterator = new PeekingIterator<String>(baseIterator);
	}
	
	@Test
	public void peekAndNextReturnSame() {
		while(peekingIterator.hasNext()) {
			String peek = peekingIterator.peek();
			String next = peekingIterator.next();
			assertEquals(peek, next);
		}
	}
	
	@Test(expected=NoSuchElementException.class)
	public void peekPastEndThrowsException() {
		while(peekingIterator.hasNext()) {
			peekingIterator.next();
		}
		peekingIterator.peek();
		fail("shouldn't get here");
	}
	
}
