/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.bytes.binarydto.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Objects;

import io.datarouter.scanner.Scanner;

public class BinaryDtoReflectionTool{

	public static <T> T newInstanceUnchecked(Class<T> cls){
		try{
			Constructor<T> constructor = cls.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}

	public static Scanner<Field> scanFieldsIncludingSuperclasses(@SuppressWarnings("rawtypes") Class type){
		return Scanner.iterate(type, cls -> cls.getSuperclass())
				.advanceUntil(Objects::isNull)
				.map(Class::getDeclaredFields)
				.concat(Scanner::of);
	}

	public static Field getField(Class<?> cls, String fieldName){
		return scanFieldsIncludingSuperclasses(cls)
				.include(field -> field.getName().equals(fieldName))
				.findFirst()
				.orElse(null);
	}

	public static void ensureAccessible(Field field, @SuppressWarnings("unused") Object object){
		if(!field.isAccessible()){
			field.setAccessible(true);
		}
	}

	public static Object getUnchecked(Field field, Object object){
		ensureAccessible(field, object);
		try{
			return field.get(object);
		}catch(IllegalArgumentException | IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}

	public static void setUnchecked(Field field, Object object, Object value){
		ensureAccessible(field, object);
		try{
			field.set(object, value);
		}catch(IllegalArgumentException | IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}

}
