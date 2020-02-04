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
package org.cast.cwm.data;

import org.cast.cwm.test.TestIdSetter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author bgoldowsky
 */
public class SiteTest {

	@Test
	public void compareByName() throws Exception {
		Site a = new Site();
		a.setName("a");

		Site b = new Site();
		b.setName("b");

		assertEquals("compareTo result is wrong", -1, a.compareTo(b));
	}

	@Test
	public void compareIsNullSafe() throws Exception {
		Site a = new Site();
		a.setName("a");

		Site b = new Site();
		TestIdSetter.setId(Site.class, a, 10L);

		assertEquals("compareTo result is wrong", 1, a.compareTo(b));
	}

	@Test
	public void compareById() throws Exception {
		Site a = new Site();
		a.setName("a");
		TestIdSetter.setId(Site.class, a, 10L);

		Site b = new Site();
		b.setName("a");
		TestIdSetter.setId(Site.class, b, 5L);

		assertEquals("compareTo result is wrong", 1, a.compareTo(b));
	}

}