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


}
