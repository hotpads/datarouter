package com.hotpads.util.core.bytes;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.util.core.ListTool;

public class BooleanByteTool{
	
	public static final int NULL = -2;

	public static byte[] getBytesNullable(Boolean value){
		if(value == null){
			return new byte[]{NULL};
		}
		return getBytes(value);
	}
	
	public static Boolean fromBytesNullable(final byte[] bytes, final int offset){
		if(Arrays.equals(bytes, new byte[]{NULL})){
			return null;
		}
		return fromBytes(bytes, offset);
	}
	
	public static byte[] getBytes(boolean value){
		return value ? new byte[]{-1} : new byte[]{0};
	}
	
	/**
	 * @return the number of bytes written to the array
	 */
	public static int toBytes(boolean value, byte[] bytes, int offset){
		bytes[offset] = value ? (byte)-1 : (byte)0;
		return 1;
	}
	
	public static boolean fromBytes(byte[] bytes, int offset){
		return bytes[offset]!=0;
	}
	
	public static byte[] getBooleanByteArray(List<Boolean> valuesWithNulls){
		if(valuesWithNulls==null){return null;}
		byte[] out = new byte[valuesWithNulls.size()];
		for(int i = 0; i < valuesWithNulls.size(); i++){
			System.arraycopy(getBytesNullable(valuesWithNulls.get(i)), 0, out, i, 1);
		}
		return out;
	}
	
	public static List<Boolean> fromBooleanByteArray(final byte[] bytes, final int startIdx){
		int numBooleans = (bytes.length - startIdx);
		List<Boolean> bools = ListTool.createArrayList();
		byte[] arrayToCopy = new byte[1];
		for(int i = 0; i < numBooleans; i++){
			System.arraycopy(bytes,  i + startIdx, arrayToCopy, 0, 1);
			bools.add(fromBytesNullable(arrayToCopy, 0));
		}
		return bools;
	}
	
	/************************** tests ************************************/
	
	public static class BooleanByteToolTests{
		@Test public void testGetBytes(){
			Assert.assertEquals(0, getBytes(false)[0]);
			Assert.assertEquals(-1, getBytes(true)[0]);
		}
		@Test public void testFromBytes(){
			Assert.assertEquals(false, fromBytes(new byte[]{0}, 0));
			Assert.assertEquals(false, fromBytes(new byte[]{1, 0}, 1));
			Assert.assertEquals(true, fromBytes(new byte[]{-37}, 0));
		}
		@Test
		public void testToFromByteArray(){
			boolean one = true;
			boolean two =  false;
			boolean three = false;
			
			List<Boolean> booleans = ListTool.create();
			booleans.add(one);
			booleans.add(null);
			booleans.add(null);
			booleans.add(two);
			booleans.add(three);
			
			byte[] booleanBytes = getBooleanByteArray(booleans);
			List<Boolean> result = fromBooleanByteArray(booleanBytes, 0);
			Assert.assertEquals(booleans, result);
			
		}
	}
}
