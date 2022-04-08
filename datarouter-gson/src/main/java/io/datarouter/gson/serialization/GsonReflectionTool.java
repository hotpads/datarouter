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
package io.datarouter.gson.serialization;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GsonReflectionTool{

	private static Set<Class<?>> getAllSuperClassesAndInterfaces(Class<?> cls){
		Set<Class<?>> supersAndInterfaces = new LinkedHashSet<>();
		for(Class<?> interfaceClass : cls.getInterfaces()){
			supersAndInterfaces.add(interfaceClass);
			supersAndInterfaces.addAll(getAllSuperClassesAndInterfaces(interfaceClass));
		}
		Class<?> superclass = cls.getSuperclass();
		if(superclass != null){
			supersAndInterfaces.add(superclass);
			supersAndInterfaces.addAll(getAllSuperClassesAndInterfaces(superclass));
		}
		return supersAndInterfaces;
	}

	/**
	 * This will return a list of all the fields declared in the super types of YourClass. It will NOT include
	 * the declared fields in YourClass.
	 * @param clazz class from which the field list will be extracted.
	 * @return a list of the inherited declared Fields not including any declared field from YourClass.
	 */
	private static List<Field> getDeclaredFieldsFromAncestors(Class<?> clazz){
		List<Field> fields = new ArrayList<>();
		for(Class<?> cls : getAllSuperClassesAndInterfaces(clazz)){
			for(Field field : cls.getDeclaredFields()){
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * This will return a list of all the fields declared in the given class and in its super types.
	 * @param clazz class from which the field list will be extracted.
	 * @return a list of the inherited declared Fields not including any declared field from YourClass.
	 */
	public static List<Field> getDeclaredFieldsIncludingAncestors(Class<?> clazz){
		List<Field> fields = new ArrayList<>();
		fields.addAll(getDeclaredFields(clazz));
		fields.addAll(getDeclaredFieldsFromAncestors(clazz));
		return fields;
	}

	/**
	 * This is a wrapper for Class.getDeclaredFields().
	 * @param cls class from which the field list will be extracted.
	 * @return a list of the declared Fields not including any inherited field.
	 */
	private static List<Field> getDeclaredFields(Class<?> cls){
		return List.of(cls.getDeclaredFields());
	}

}
