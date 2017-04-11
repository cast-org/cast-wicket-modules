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
package org.cast.cwm.data;

import org.cast.cwm.test.TestIdSetter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author bgoldowsky
 */
public class PeriodTest {

	Site siteA;
	Site siteB;

	@Before
	public void setUp() {
		siteA = new Site();
		siteA.setName("a");

		siteB = new Site();
		siteB.setName("b");

	}

	@Test
	public void compareByPeriod() throws Exception {
		Period a = new Period();
		a.setSite(siteA);
		a.setName("test");

		Period b = new Period();
		b.setSite(siteB);
		b.setName("test");

		assertEquals("compareTo result is wrong", -1, a.compareTo(b));
	}

	@Test
	public void compareByName() throws Exception {
		Period a = new Period();
		a.setSite(siteA);
		a.setName("a");

		Period b = new Period();
		b.setSite(siteA);
		b.setName("b");

		assertEquals("compareTo result is wrong", -1, a.compareTo(b));
	}

	@Test
	public void compareIsNullSafe() throws Exception {
		Period a = new Period();
		a.setSite(siteA);

		Period b = new Period();
		b.setName("b");

		assertEquals("compareTo result is wrong", 1, a.compareTo(b));
	}

	@Test
	public void compareById() throws Exception {
		Period a = new Period();
		a.setName("a");
		TestIdSetter.setId(Period.class, a, 10L);

		Period b = new Period();
		b.setName("a");
		TestIdSetter.setId(Period.class, b, 5L);

		assertEquals("compareTo result is wrong", 1, a.compareTo(b));
	}

	@Test
	public void compareEquals() {
		Period a = new Period();
		a.setName("a");
		a.setSite(siteA);

		assertEquals("compareTo result is wrong", 0, a.compareTo(a));
	}

}