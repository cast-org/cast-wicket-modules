package org.cast.cwm.test;

import java.lang.reflect.Field;

public abstract class TestIdSetter {

	public static <T> void setId(
			Class<T> clazz, 
			T object, 
			Long id) {
		try {
			Field field = clazz.getDeclaredField("id");
			field.setAccessible(true);
			field.set(object, id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
