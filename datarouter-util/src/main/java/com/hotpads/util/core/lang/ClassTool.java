package com.hotpads.util.core.lang;

import org.testng.Assert;
import org.testng.annotations.Test;

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

	public static boolean isEquivalentBoxedType(Class<?> classA, Class<?> classB){
		return isEquivalentSingleBoxedType(classA, classB, boolean.class, Boolean.class)
				|| isEquivalentSingleBoxedType(classA, classB, byte.class, Byte.class)
				|| isEquivalentSingleBoxedType(classA, classB, short.class, Short.class)
				|| isEquivalentSingleBoxedType(classA, classB, char.class, Character.class)
				|| isEquivalentSingleBoxedType(classA, classB, int.class, Integer.class)
				|| isEquivalentSingleBoxedType(classA, classB, float.class, Float.class)
				|| isEquivalentSingleBoxedType(classA, classB, long.class, Long.class)
				|| isEquivalentSingleBoxedType(classA, classB, double.class, Double.class);
	}

	private static boolean isEquivalentSingleBoxedType(Class<?> classA, Class<?> classB, Class<?> primitive,
			Class<?> boxed){
		return primitive.equals(classA) && primitive.equals(classB)
				|| primitive.equals(classA) && boxed.equals(classB)
			    || boxed.equals(classA) && primitive.equals(classB)
				|| boxed.equals(classA) && boxed.equals(classB);
	}


	/**************** Tests *************************/

	public static class ClassToolTests{
		@Test
		public void testIsEquivalentBoxedType(){
			Assert.assertTrue(isEquivalentBoxedType(int.class, int.class));
			Assert.assertTrue(isEquivalentBoxedType(int.class, Integer.class));
			Assert.assertTrue(isEquivalentBoxedType(Integer.class, int.class));
			Assert.assertTrue(isEquivalentBoxedType(Integer.class, Integer.class));

			Assert.assertFalse(isEquivalentBoxedType(long.class, int.class));
			Assert.assertFalse(isEquivalentBoxedType(long.class, Integer.class));
			Assert.assertFalse(isEquivalentBoxedType(String.class, String.class));//not primitive
		}
	}
}
