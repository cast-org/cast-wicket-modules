/*
 * Copyright 2011 CAST, Inc.
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
package org.cast.cwm.tag;

import junit.framework.TestCase;

import org.cast.cwm.data.User;
import org.junit.Test;


public class TestTagService extends TestCase {
	
	TagService ts;
	
	@Override
	public void setUp()
	{
		 ts = new TagService();
	}

	@Test
	public void testConfigureTaggableClass() {
		ts.configureTaggableClass('U', User.class);
		assertEquals(ts.getTaggableType('U'), User.class);
		assertEquals((Character)'U', ts.getTypeCode(User.class));
	}

	@Test
	public void testCleanTagName() {
		assertEquals(TagService.cleanTagName("tag"), "tag");
		assertEquals(TagService.cleanTagName("5\u2605*"), "5**");
		assertEquals(TagService.cleanTagName("  - a-b  - c_de - "), "a_b_c_de");
		assertNull(TagService.cleanTagName("--"));
		assertNull(TagService.cleanTagName(""));
	}

	
}
