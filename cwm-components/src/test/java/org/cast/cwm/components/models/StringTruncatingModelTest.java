/*
 * Copyright 2011-2018 CAST, Inc.
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
package org.cast.cwm.components.models;

import static org.junit.Assert.*;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.junit.Before;
import org.junit.Test;

public class StringTruncatingModelTest {
	
	private IModel<String> delegate;
	
	@Before
	public void setUp () {
		delegate = new Model<String>("0123456789-123456789");
	}
	
	@Test
	public void passesThroughNullModelObject () {
		delegate.setObject(null);
		StringTruncatingModel model = new StringTruncatingModel(delegate, 10, "...");
		assertNull(model.getObject());
	}

	@Test
	public void passesThroughEmptyString () {
		delegate.setObject("");
		StringTruncatingModel model = new StringTruncatingModel(delegate, 10, "...");
		assertEquals("", model.getObject());
	}

	// Without word boundary detection
	
	@Test
	public void rendersFullStringWhenLessThanMaxLength () {
		StringTruncatingModel model = new StringTruncatingModel(delegate, 25);
		model.setBreakAtWordBoundary(false);
		assertEquals(delegate.getObject(), model.getObject());
	}
	
	@Test
	public void rendersCorrectLengthWithDefaultSuffix () {
		StringTruncatingModel model = new StringTruncatingModel(delegate, 10);
		model.setBreakAtWordBoundary(false);
		assertEquals("012345678…", model.getObject());
	}
	
	@Test
	public void rendersCorrectLengthWithSpecifiedSuffix () {
		StringTruncatingModel model = new StringTruncatingModel(delegate, 10, "...");
		model.setBreakAtWordBoundary(false);
		assertEquals("0123456...", model.getObject());
	}
	
	@Test
	public void normalizesSpaces () {
		delegate.setObject("   one   two         three   four  five   ");
		StringTruncatingModel model = new StringTruncatingModel(delegate, 13, "...");
		model.setBreakAtWordBoundary(false);
		assertEquals("one two th...", model.getObject());
	}

	@Test
	public void normalizesWhitespace () {
		delegate.setObject(" \t\n  one \r\r  two\t\t         three   four  five   ");
		StringTruncatingModel model = new StringTruncatingModel(delegate, 13, "...");
		model.setBreakAtWordBoundary(false);
		assertEquals("one two th...", model.getObject());
	}

	// With word boundary detection
	
	@Test
	public void wbRendersFullStringWhenLessThanMaxLength () {
		StringTruncatingModel model = new StringTruncatingModel(delegate, 25);
		assertEquals(delegate.getObject(), model.getObject());
	}
		
	@Test
	public void wbFindsWordBoundary () {
		delegate.setObject("one two three four five");
		StringTruncatingModel model = new StringTruncatingModel(delegate, 13, "...");
		assertEquals("one two ...", model.getObject());
	}

	@Test
	public void wbNormalizesSpaces () {
		delegate.setObject("   one   two         three   four  five   ");
		StringTruncatingModel model = new StringTruncatingModel(delegate, 13, "...");
		assertEquals("one two ...", model.getObject());
	}

	@Test
	public void wbFindsLastWordBoundary () {
		delegate.setObject("one two three four five");
		StringTruncatingModel model = new StringTruncatingModel(delegate, 10, "...");
		assertEquals("one two...", model.getObject());
	}

	@Test
	public void wbDealsWithLongWords () {
		delegate.setObject("one antidisestablishmentarianism");
		StringTruncatingModel model = new StringTruncatingModel(delegate, 10, "...");
		assertEquals("one ant...", model.getObject());
	}

	@Test
	public void wbDealsWithNoWordBreaks () {
		delegate.setObject("onetwothreefourfive");
		StringTruncatingModel model = new StringTruncatingModel(delegate, 10, "...");
		assertEquals("onetwot...", model.getObject());
	}

	@Test
	public void wbFindsPunctuationBasedWordBoundary () {
		delegate.setObject("one,two,three,four,five");
		StringTruncatingModel model = new StringTruncatingModel(delegate, 10, "...");
		assertEquals("one,two...", model.getObject());
	}

	@Test
	public void wbFindsWhitespaceBasedWordBoundary () {
		delegate.setObject("one\ntwo\nthree\nfour\nfive");
		StringTruncatingModel model = new StringTruncatingModel(delegate, 10, "...");
		assertEquals("one\ntwo...", model.getObject());
	}


}
