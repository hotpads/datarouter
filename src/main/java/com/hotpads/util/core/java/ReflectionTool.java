package com.hotpads.util.core.java;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Unsafe;

import com.google.common.collect.Lists;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrGenericsFactory;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrSetTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class ReflectionTool {
	private static Logger logger = LoggerFactory.getLogger(ReflectionTool.class);
	
	@SuppressWarnings("unchecked") 
	public static <T> T create(String fullyQualifiedClassName){
		Class<T> cls;
		try{
			cls = (Class<T>)Class.forName(fullyQualifiedClassName);
		}catch(ClassNotFoundException e){
			throw new RuntimeException(fullyQualifiedClassName, e);
		}
		return create(cls);
	}
	
	public static <T> T create(Class<T> cls, String exceptionMessage){
		try{
			if(cls.isEnum()){ 
				T e = cls.getEnumConstants()[0];
				if(e==null){ throw new IllegalArgumentException("no values in enum class:"+cls.getName()); }
				return e;
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
	
	public static <E extends Enum<E>> E createEnum(Class<E> enumClass){
		try{
			E e = enumClass.getEnumConstants()[0];
			if(e==null){ throw new IllegalArgumentException(enumClass.getName()+" has no values"); }
			return e;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
    }
	
	public static Object get(String fieldName, Object object){
		return get(getCachedDeclaredFieldFromHierarchy(object.getClass(), fieldName), object);
	}
	
	public static Object get(Field field, Object object){
		if(field != null && object != null){
			try{
				field.setAccessible(true);
				return field.get(object);
			}catch(IllegalArgumentException e){
				throw new RuntimeException(e);
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	public static void set(Field field, Object object, Object value){
		try{
			field.set(object, value);
		}catch(IllegalArgumentException e){
			throw new RuntimeException(e);
		}catch(IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}
	
	public static Set<Class<?>> getAllSuperClassesAndInterfaces(Class<?> c){
		Set<Class<?>> supersAndInterfaces = DrGenericsFactory.makeHashSet();
		
		List<Class<?>> interfaces = Arrays.asList(c.getInterfaces());
		if(interfaces!=null){
			for(Class<?> i:interfaces){
				supersAndInterfaces.add(i);
				supersAndInterfaces.addAll(getAllSuperClassesAndInterfaces(i));
			}
		}
		
		Class<?> superclass = c.getSuperclass();
		if(superclass != null){
			supersAndInterfaces.add(superclass);
			supersAndInterfaces.addAll(getAllSuperClassesAndInterfaces(superclass));		
		}
		return supersAndInterfaces;
	}
	
	public static Set<Class<?>> getAllSubClassesAnd(Class<?> c){
		Class<?>[] subClass = c.getClasses();
		return DrSetTool.create(subClass);
	}
	
	/************************** names *******************************/
	
	public static List<String> getAllHierarchyFieldNames(Class<?> c){
		List<String> names = DrListTool.create();
		for(Class<?> cls : getAllSuperClassesAndInterfaces(c)){
			for(Field field : cls.getDeclaredFields()){
				names.add(field.getName());
			}
		}
		return names;
	}
	
	public static List<String> getMethodNames(Class<?> c){
		List<String> names = Lists.newArrayList();
		for(Method m : c.getMethods())
			names.add(m.getName());
		return names;
	}
	
	
	/*********************** get fields ***********************************/
	
	public static Field getDeclaredFieldFromHierarchy(Class<?> c, String fieldName){
		Class<?> cls = c;
		while(!cls.equals(Object.class)){
			Field superField;
			try{
				superField = cls.getDeclaredField(fieldName);
			}catch(NoSuchFieldException nsfe){
				cls = cls.getSuperclass();
				continue;
			}
			superField.setAccessible(true);
			return superField;
		}
		return null;
	}
	
	private static class FieldInClass {
		public Class<?> c;
		public String fieldName;
		
		public FieldInClass(Class<?> c, String fieldName){
			this.c = c;
			this.fieldName = fieldName;
		}
		
		@Override
		public int hashCode(){
			final int prime = 31;
			int result = 1;
			result = prime * result + ((c == null) ? 0 : c.hashCode());
			result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj){
			if(obj instanceof FieldInClass){
				FieldInClass o = (FieldInClass)obj;
				return o.fieldName.equals(fieldName) && o.c.equals(c);
			}
			return false;
		}
	}
	
	private static ConcurrentHashMap<FieldInClass, Field> cachedDeclaredFields = new ConcurrentHashMap<>();
	
	public static Field getCachedDeclaredFieldFromHierarchy(Class<?> c, String fieldName){
		FieldInClass fieldInClass = new FieldInClass(c, fieldName);
		Field field = cachedDeclaredFields.get(fieldInClass);
		if(field == null){
			field = getDeclaredFieldFromHierarchy(c, fieldName);
			cachedDeclaredFields.put(fieldInClass, field);
		}
		return field;
	}
	
	public static java.lang.reflect.Field getNestedField(
			Object object, List<String> fieldNames){
		try{
			String fieldName = DrCollectionTool.getFirst(fieldNames);
			java.lang.reflect.Field field = ReflectionTool.getDeclaredFieldFromHierarchy(
					object.getClass(), fieldName);
			field.setAccessible(true);
			if(DrCollectionTool.size(fieldNames)==1){ return field; }
			if(field.get(object)==null){//initialize the field
				field.set(object, ReflectionTool.create(field.getType()));
			}
			return getNestedField(field.get(object), 
					fieldNames.subList(1, fieldNames.size()));
		}catch(Exception e){
			String message = "could not set field: "+object.getClass().getName()+"."
					+DrStringTool.concatenate(fieldNames, ".");
			throw new RuntimeException(message, e);
		}
	}
	
	public static List<Field> getAllHierarchyFields(Class<?> c){
		List<String> allFieldsName = getAllHierarchyFieldNames(c);
		List<Field> toReturn = DrListTool.create();
		for(String fieldName : allFieldsName){
			toReturn.add(getDeclaredFieldFromHierarchy(c, fieldName));
		}
		return toReturn;
	}
	
	public static List<Field> getAllFields(Class<?> c){
		return DrListTool.create(c.getDeclaredFields());
	}
	
	
	/*********************** get Method ***********************************/
	
	public static Method getDeclaredMethodFromHierarchy(Class<?> c, String methodName){
		try{
			Method method = c.getDeclaredMethod(methodName);
			if(method!=null){ 
				method.setAccessible(true);
				return method; 
			}
		}catch(NoSuchMethodException nsfe){
		}
		for(Class<?> cls : getAllSuperClassesAndInterfaces(c)){
			try{
				Method superMethod = cls.getDeclaredMethod(methodName);
				if(superMethod!=null){ return superMethod; }
			}catch(NoSuchMethodException nsfe){
			}
		}
		return null;
	}
	
	public static <T> Collection<Method> getDeclaredMethodsWithName(Class<T> c, String methodName){
		Collection<Method> methods = DrListTool.create();
		Class<?> clazz = c;
		do{
			for(Method method : clazz.getDeclaredMethods()){
				if(method.getName().equals(methodName)){
					method.setAccessible(true);
					methods.add(method);
				}
			}
		}while((clazz = clazz.getSuperclass()) != null);
		return methods;
	}
	

	/*********************** get Value *********************************************************/
	public static Object getValueFromMethod(Method method, Object methodClass, Object args){
		Object toReturn = null;
		try{
			toReturn = method.invoke(methodClass, args);

		}catch(IllegalAccessException e){
			logger.warn("Illegal access exception "+ method , e);
		}catch(IllegalArgumentException e){
			logger.warn("IllegalArgumentException "  +args, e);
		}catch(InvocationTargetException e){
			logger.warn("InvocationTargetException "  +methodClass, e);
		}
		return toReturn;
	}
	
	public static Object getValueFromMethod(Method method, Object instance){
		Object toReturn = null;
		try{
			if(instance!=null && method !=null){
				method.setAccessible(true);
				toReturn = method.invoke(instance);
			}	

		}catch(IllegalAccessException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IllegalArgumentException e){
			logger.error("the method " + method.toString() + " or " + instance.toString() + " are not a suitable argument ", e);
		}catch(InvocationTargetException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return toReturn;
	}
	
	public static Object getObjectValueUsingGetterMethod( Object objectInstance,Method getter){
				return ReflectionTool.getValueFromMethod(getter, objectInstance);
	}
	
	/************************ for experiments only ******************************/
	
	//need to do this stuff to avoid getting a SecurityException
	//  from: http://www.thatsjava.com/java-programming/43398/
	public static Unsafe getUnsafe(){
		Unsafe unsafe = null;
		try{
			Class<Unsafe> uc = Unsafe.class;
			Field[] fields = uc.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].getName().equals("theUnsafe")) {
					fields[i].setAccessible(true);
					unsafe = (Unsafe) fields[i].get(uc);
					break;
				}
			}
		}catch (Exception ignore){
		}
		return unsafe;
	}
	
	
	/*************************** main ********************************************/
		
	public static void main(String[] args){
		Set<Class<?>> sais = getAllSuperClassesAndInterfaces(ArrayList.class);
		for(Class<?> c:sais){
			System.err.println(c);
		}
	}

	
}
