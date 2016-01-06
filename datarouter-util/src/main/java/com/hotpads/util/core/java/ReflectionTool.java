package com.hotpads.util.core.java;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class ReflectionTool {
	private static final Logger logger = LoggerFactory.getLogger(ReflectionTool.class);

	public static <T> Supplier<T> supplier(Class<T> type){
		return () -> ReflectionTool.create(type);
	}

	public static <T> T createAsSubclass(String className, Class<T> superClass){
		return create(getClass(className).asSubclass(superClass));
	}

	private static Class<?> getClass(String className){
		try{
			return Class.forName(className);
		}catch(ClassNotFoundException e){
			throw new RuntimeException(className, e);
		}
	}

	public static <T,U> T createWithParameters(Class<T> type, Collection<U> requiredParameters){
		candidateConstructorLoop:
		for(Constructor<?> constructor : type.getDeclaredConstructors()){
			if (constructor.getParameterCount() != requiredParameters.size()) {
				continue;
			}
			Iterator<U> requiredParametersIterator = requiredParameters.iterator();
			for(Class<?> constructorParameterType : constructor.getParameterTypes()){
				if(!constructorParameterType.isAssignableFrom(requiredParametersIterator.next().getClass())){
					continue candidateConstructorLoop;
				}
			}
			try{
				return type.cast(constructor.newInstance(requiredParameters.toArray()));
			}catch (SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e){
				throw new RuntimeException(e);
			}
		}
		throw new RuntimeException("Could not find constructor " + type.getCanonicalName() + requiredParameters);
	}

	public static Object create(String fullyQualifiedClassName){
		return create(getClass(fullyQualifiedClassName));
	}

	public static <T> T create(Class<T> cls, String exceptionMessage){
		try{
			if(cls.isEnum()){
				T enumValue = cls.getEnumConstants()[0];
				if(enumValue == null){
					throw new IllegalArgumentException("no values in enum class:"+cls.getName());
				}
				return enumValue;
			}
			//use getDeclaredConstructor to access non-public constructors
			Constructor<T> constructor = cls.getDeclaredConstructor();
			constructor.setAccessible(true);
			T databeanInstance = constructor.newInstance();
			return databeanInstance;
		}catch(Exception e){
			if(exceptionMessage == null){
				throw new RuntimeException(e);
			}
			throw new RuntimeException(exceptionMessage, e);
		}
	}

	public static <T> T create(Class<T> cls){
		return create(cls, null);
	}

	public static Object get(String fieldName, Object object){
		return get(getCachedDeclaredFieldFromHierarchy(object.getClass(), fieldName), object);
	}

	public static Object get(Field field, Object object){
		if(field != null && object != null){
			field.setAccessible(true);
			try{
				return field.get(object);
			}catch(IllegalArgumentException|IllegalAccessException e){
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public static void set(Field field, Object object, Object value){
		try{
			field.set(object, value);
		}catch(IllegalArgumentException|IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}

	public static Set<Class<?>> getAllSuperClassesAndInterfaces(Class<?> cls){
		Set<Class<?>> supersAndInterfaces = new HashSet<>();
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

	/*********************** get fields ***********************************/

	public static Field getDeclaredFieldFromHierarchy(Class<?> clazz, String fieldName){
		Set<Class<?>> supersAndInterfaces = getAllSuperClassesAndInterfaces(clazz);
		supersAndInterfaces.add(clazz);
		for(Class<?> cls : supersAndInterfaces){
			Field superField;
			try{
				superField = cls.getDeclaredField(fieldName);
			}catch(NoSuchFieldException nsfe){
				continue;
			}
			superField.setAccessible(true);
			return superField;
		}
		return null;
	}

	private static class FieldInClass {
		public Class<?> cls;
		public String fieldName;

		public FieldInClass(Class<?> cls, String fieldName){
			this.cls = cls;
			this.fieldName = fieldName;
		}

		@Override
		public int hashCode(){
			final int prime = 31;
			int result = 1;
			result = prime * result + (cls == null ? 0 : cls.hashCode());
			result = prime * result + (fieldName == null ? 0 : fieldName.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj){
			if(obj instanceof FieldInClass){
				FieldInClass other = (FieldInClass)obj;
				return other.fieldName.equals(fieldName) && other.cls.equals(cls);
			}
			return false;
		}
	}

	private static ConcurrentHashMap<FieldInClass, Field> cachedDeclaredFields = new ConcurrentHashMap<>();

	public static Field getCachedDeclaredFieldFromHierarchy(Class<?> cls, String fieldName){
		FieldInClass fieldInClass = new FieldInClass(cls, fieldName);
		Field field = cachedDeclaredFields.get(fieldInClass);
		if(field == null){
			field = getDeclaredFieldFromHierarchy(cls, fieldName);
			cachedDeclaredFields.put(fieldInClass, field);
		}
		return field;
	}

	public static Field getNestedField(Object object, List<String> fieldNames){
		try{
			String fieldName = DrCollectionTool.getFirst(fieldNames);
			Field field = getDeclaredFieldFromHierarchy(object.getClass(), fieldName);
			field.setAccessible(true);
			if(DrCollectionTool.size(fieldNames)==1){
				return field;
			}
			if(field.get(object)==null){//initialize the field
				field.set(object, create(field.getType()));
			}
			return getNestedField(field.get(object), fieldNames.subList(1, fieldNames.size()));
		}catch(Exception e){
			String message = "could not set field: " + object.getClass().getName() + "."
					+ DrStringTool.concatenate(fieldNames, ".");
			throw new RuntimeException(message, e);
		}
	}

	public static List<Field> getAllHierarchyFields(Class<?> clazz){
		List<Field> fields = new ArrayList<>();
		for(Class<?> cls : getAllSuperClassesAndInterfaces(clazz)){
			for(Field field : cls.getDeclaredFields()){
				fields.add(field);
			}
		}
		return fields;
	}

	public static List<Field> getAllFields(Class<?> cls){
		return Arrays.asList(cls.getDeclaredFields());
	}

	/*********************** get Method ***********************************/

	public static Method getDeclaredMethodFromHierarchy(Class<?> clazz, String methodName){
		try{
			Method method = clazz.getDeclaredMethod(methodName);
			if(method!=null){
				method.setAccessible(true);
				return method;
			}
		}catch(NoSuchMethodException nsfe){
			//continue
		}
		for(Class<?> cls : getAllSuperClassesAndInterfaces(clazz)){
			try{
				Method superMethod = cls.getDeclaredMethod(methodName);
				if(superMethod!=null){
					return superMethod;
				}
			}catch(NoSuchMethodException nsfe){
				//continue
			}
		}
		return null;
	}

	public static <T> Collection<Method> getDeclaredMethodsWithName(Class<T> cls, String methodName){
		List<Method> methods = new ArrayList<>();
		Set<Class<?>> supersAndInterfaces = getAllSuperClassesAndInterfaces(cls);
		supersAndInterfaces.add(cls);
		for(Class<?> clazz : supersAndInterfaces){
			for(Method method : clazz.getDeclaredMethods()){
				if(method.getName().equals(methodName)){
					method.setAccessible(true);
					methods.add(method);
				}
			}
		}
		return methods;
	}

	/*********************** get Value *********************************************************/
	public static Object getObjectValueUsingGetterMethod(Object instance, Method method){
		if(instance == null || method == null){
			return null;
		}
		try{
			method.setAccessible(true);
			return method.invoke(instance);
		}catch(IllegalAccessException | InvocationTargetException e){
			logger.error("", e);
		}catch(IllegalArgumentException e){
			logger.error("the method " + method + " or " + instance + " are not a suitable argument ", e);
		}
		return null;
	}
}
