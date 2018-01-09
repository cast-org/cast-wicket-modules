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
package org.cast.cwm.test;

import java.lang.reflect.Field;

import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;

public class TestDataUtil {

	public static void setId(Object item, long id) {
		setId(item, id, item.getClass());
	}

	//field is in superclass, so we explicitly specify class.  Could probably make it generic by searching hierarchy for field...
	public static void setId(User user, long id) {
		setId(user, id, User.class);
	}

	public static void setId(Object item, long id, Class<?> clazz) {
		Field field;
		try {
			field = clazz.getDeclaredField("id");
			field.setAccessible(true);
			field.set(item, id);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set ID", e);
		}
	}

	public static User makeUser(Long id, String first, String last, Role role) {
		User user = new User();
		setId(user, id);
		user.setFirstName(first);
		user.setLastName(last);
		user.setRole(role);
		return user;
	}

	public static User makeUser(Long id, String first, String last, Role role, String username) {
		User user = makeUser(id, first, last, role);
		user.setUsername(username);
		return user;
	}
}
