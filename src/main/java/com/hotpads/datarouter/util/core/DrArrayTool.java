package com.hotpads.datarouter.util.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DrArrayTool {
	
	public static <T> T getFirst(T[] ins){
		return isEmpty(ins) ? null : ins[0];
	}

	public static void copyWrapException(Object src, int srcPos, Object dest, int destPos, int length){
		try{
			System.arraycopy(src, srcPos, dest, destPos, length);
		}catch(RuntimeException e){
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] clone(byte[] in){
		if(in==null){ return null; }
		byte[] out = new byte[in.length];
		System.arraycopy(in, 0, out, 0, in.length);
		return out;
	}
	
	public static byte[] nullSafe(byte[] a){
		if(a==null){ return new byte[0]; }
		return a;
	}

	public static String[] nullSafe(String[] a){
		if(a==null){ return new String[0]; }
		return a;
	}
	
	public static int length(byte[] a){
		if(a==null){ return 0; }
		return a.length;
	}
	
	
	//TODO remove the "nullSafe" from method names
	public static int nullSafeLength(short[] a){
		if(a==null){ return 0; }
		return a.length;
	}

	public static int nullSafeLength(int[] a){
		if(a==null){ return 0; }
		return a.length;
	}

	public static int nullSafeLength(long[] a){
		if(a==null){ return 0; }
		return a.length;
	}

	public static int nullSafeLength(Object[] a){
		if(a==null){ return 0; }
		return a.length;
	}
	
	public static boolean isEmpty(byte[] a){
		if(a==null){ return true; }
		if(a.length==0){ return true; }
		return false;
	}
	
	public static boolean isEmpty(short[] a){
		if(a==null){ return true; }
		if(a.length==0){ return true; }
		return false;
	}
	
	public static boolean isEmpty(int[] a){
		if(a==null){ return true; }
		if(a.length==0){ return true; }
		return false;
	}
	
	public static boolean isEmpty(Object[] a){
		if(a==null){ return true; }
		if(a.length==0){ return true; }
		return false;
	}
	
	public static boolean notEmpty(Object[] a){
		return ! isEmpty(a);
	}
	
	/*
	 * concatenate two arrays of bytes 
	 */
	public static byte[] concatenate(byte[] A, byte[] B) {
		   byte[] C= new byte[A.length+B.length];
		   System.arraycopy(A, 0, C, 0, A.length);
		   System.arraycopy(B, 0, C, A.length, B.length);
		   return C;
		}
	
	
	public static boolean containsUnsorted(byte[] a, byte key){
		if(isEmpty(a)){ return false; }
		for(int i=0; i < a.length; ++i){
			if(a[i]==key){ return true; }
		}
		return false;
	}
	
	public static boolean containsUnsorted(int[] a, Integer key){
		if(key==null){ return false; }
		if(isEmpty(a)){ return false; }
		for(int i=0; i < a.length; ++i){
			if(a[i]==key){ return true; }
		}
		return false;
	}
	
	public static String toCsvString(byte[] a){
		if(a==null){ return ""; }
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < a.length; ++i){
			if(i>0){ sb.append(","); }
			sb.append(a[i]);
		}
		return sb.toString();
	}
	
	public static String toCsvString(double[] a){
		if(a==null){ return ""; }
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < a.length; ++i){
			if(i>0){ sb.append(","); }
			sb.append(a[i]);
		}
		return sb.toString();
	}
	
	public static long[] primitiveLongArray(Collection<Long> ins){
		if(DrCollectionTool.isEmpty(ins)){ return new long[0]; }
		long[] array = new long[ins.size()];
		int index = 0;
		for(long i : ins){
			array[index++] = i;
		}
		return array;
	}
	
	public static void copyInto(byte[] destination, byte[] source, int offset) {
		for(int i = 0; i < source.length; i++){
			try{
				destination[offset + i] = source[i];
			}
			catch(ArrayIndexOutOfBoundsException e){
				throw new ArrayIndexOutOfBoundsException("array1 + offset is shorter than array2");
			}
		}
	}

	 public static class Tests{
		 @Test public void simpleCompare(){
			 Double one = 1.0;
			 Double two = 2.0;
			 Assert.assertEquals(0, one.compareTo(one));
			 Assert.assertEquals(-1, one.compareTo(two));
			 Assert.assertEquals(1, two.compareTo(one));
		 }
	 }


}
















