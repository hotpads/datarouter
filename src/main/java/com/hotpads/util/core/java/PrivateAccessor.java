package com.hotpads.util.core.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * this is mostly for AnnotationTool... kind of breaks java
 * @author david
 *
 */
public class PrivateAccessor {

	public static Field getPrivateField (Class c, String fieldName) {
		if(c==null || fieldName==null) return null;
		final Field[] fields = c.getDeclaredFields();
		for (int i = 0; i < fields.length; ++i) {
			if (fieldName.equals(fields[i].getName())) {
				fields[i].setAccessible(true);
				return fields[i];
			}
		}
		return null;
	}
	
	public static Method getPrivateMethod (Class c, String methodName) {
		if(c==null || methodName==null) return null;
		final Method[] methods = c.getDeclaredMethods();
		for (int i = 0; i < methods.length; ++i) {
			if (methodName.equals(methods[i].getName())) {
				methods[i].setAccessible(true);
				return methods[i];
			}
		}
		return null;
	}
	
}
