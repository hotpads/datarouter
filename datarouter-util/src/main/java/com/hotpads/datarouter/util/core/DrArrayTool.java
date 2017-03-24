package com.hotpads.datarouter.util.core;

import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DrArrayTool{

	public static byte[] clone(byte[] in){
		if(in == null){
			return null;
		}
		byte[] out = new byte[in.length];
		System.arraycopy(in, 0, out, 0, in.length);
		return out;
	}

	public static byte[] nullSafe(byte[] array){
		if(array == null){
			return new byte[0];
		}
		return array;
	}

	public static String[] nullSafe(String[] array){
		if(array == null){
			return new String[0];
		}
		return array;
	}

	public static int length(byte[] array){
		if(array == null){
			return 0;
		}
		return array.length;
	}

	public static int length(Object[] array){
		if(array == null){
			return 0;
		}
		return array.length;
	}

	public static boolean isEmpty(byte[] array){
		if(array == null){
			return true;
		}
		if(array.length == 0){
			return true;
		}
		return false;
	}

	public static boolean isEmpty(Object[] array){
		if(array == null){
			return true;
		}
		if(array.length == 0){
			return true;
		}
		return false;
	}

	public static boolean notEmpty(Object[] array){
		return !isEmpty(array);
	}

	public static boolean notEmpty(byte[] array){
		return !isEmpty(array);
	}

	public static byte[] concatenate(byte[]... arrays){
		int totalLength = 0;
		for(int i = 0; i < length(arrays); ++i){
			totalLength += length(arrays[i]);
		}
		byte[] result = new byte[totalLength];
		int nextStartIndex = 0;
		for(int i = 0; i < length(arrays); ++i){
			int argArrayLength = length(arrays[i]);
			if(argArrayLength > 0){
				System.arraycopy(arrays[i], 0, result, nextStartIndex, argArrayLength);
				nextStartIndex += argArrayLength;
			}
		}
		return result;
	}

	public static boolean containsUnsorted(byte[] array, byte key){
		if(isEmpty(array)){
			return false;
		}
		for(int i = 0; i < array.length; ++i){
			if(array[i] == key){
				return true;
			}
		}
		return false;
	}

	public static long[] primitiveLongArray(Collection<Long> ins){
		if(DrCollectionTool.isEmpty(ins)){
			return new long[0];
		}
		long[] array = new long[ins.size()];
		int index = 0;
		for(long i : ins){
			array[index++] = i;
		}
		return array;
	}

	public static class Tests{
		@Test
		public void simpleCompare(){
			Double one = 1.0;
			Double two = 2.0;
			Assert.assertEquals(one.compareTo(one), 0);
			Assert.assertEquals(one.compareTo(two), -1);
			Assert.assertEquals(two.compareTo(one), 1);
		}

		@Test
		public void testConcatenateVarargBytes(){
			byte[] concat = concatenate(new byte[]{0, 1}, new byte[]{2}, new byte[]{3, 4});
			Assert.assertEquals(concat, new byte[]{0, 1, 2, 3, 4});
		}
	}

}
