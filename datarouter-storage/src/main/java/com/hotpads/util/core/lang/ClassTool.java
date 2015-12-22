package com.hotpads.util.core.lang;

import com.hotpads.datarouter.util.core.DrStringTool;

public class ClassTool {

	public static boolean sameClass(Object objectA, Object objectB){
		if(objectA == null && objectB == null){
			return true;
		}
		if(objectA == null && objectB != null){
			return false;
		}
		if(objectA != null && objectB == null){
			return false;
		}
		return objectA.getClass().equals(objectB.getClass());
	}

	public static boolean sameClass(Class<?> classA, Class<?> classB){
		if(classA==null && classB==null){
			return true;
		}
		if(classA==null && classB!=null){
			return false;
		}
		if(classA!=null && classB==null){
			return false;
		}
		return classA.equals(classB);
	}

	public static boolean differentClass(Object objectA, Object objectB){
		return !sameClass(objectA, objectB);
	}

	public static int compareClass(Object objectA, Object objectB){
		if(objectA==objectB){
			return 0;
		}
		if(objectA==null){
			return -1;
		}
		if(objectB==null){
			return 1;
		}
		return objectA.getClass().getName().compareTo(objectB.getClass().getName());
	}

	public static String getSimpleNameFromCanonicalName(String canonicalClassName){
		return DrStringTool.getStringAfterLastOccurrence('.', canonicalClassName);
	}

	public static String getPackageFromCanonicalName(String canonicalClassName){
		return DrStringTool.getStringBeforeLastOccurrence('.', canonicalClassName);
	}

	public static Class<?> forName(String className){
		try{
			return Class.forName(className);
		}catch(ClassNotFoundException e){
			throw new IllegalArgumentException(e);
		}
	}

}
