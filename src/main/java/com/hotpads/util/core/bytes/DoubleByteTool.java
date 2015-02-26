package com.hotpads.util.core.bytes;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.util.core.ByteTool;
import com.hotpads.datarouter.util.core.ListTool;

public class DoubleByteTool{
	
	public static final long NaN = 0x0010000000000000L;

	public static Double fromBytesNullable(final byte[] bytes, final int offset){
		Long longValue = LongByteTool.fromRawBytes(bytes, offset);
		if(longValue.longValue() == NaN){
			return null;
		}
		return Double.longBitsToDouble(longValue);
	}
	
	public static byte[] getBytesNullable(Double in){
		if(in == null){
			return getBytes(Double.longBitsToDouble(NaN));
		}
		return getBytes(in);
	}
	
	public static byte[] getBytes(final double in){
		long bits = Double.doubleToLongBits(in);
		return LongByteTool.getRawBytes(bits);
	}
	
	public static int toBytes(final double in, final byte[] bytes, final int offset){
		long bits = Double.doubleToLongBits(in);
		LongByteTool.toRawBytes(bits, bytes, offset);
		return 8;
	}

	public static double fromBytes(final byte[] bytes, final int offset){
		return Double.longBitsToDouble(LongByteTool.fromRawBytes(bytes, offset));
	}
	
	public static List<Double> fromDoubleByteArray(final byte[] bytes, final int startIdx){
		int numDoubles = (bytes.length - startIdx)/8;
		List<Double> doubles = ListTool.createArrayList();
		byte[] arrayToCopy = new byte[8];
		for(int i = 0; i < numDoubles; i++){
			System.arraycopy(bytes, i * 8 + startIdx, arrayToCopy, 0, 8);
			doubles.add(fromBytesNullable(arrayToCopy, 0));
		}
		return doubles;
	}
	
	public static byte[] getDoubleByteArray(List<Double> valuesWithNulls){
		if(valuesWithNulls==null){ return null; }
		byte[] out = new byte[8*valuesWithNulls.size()];
		for(int i=0; i < valuesWithNulls.size(); ++i){
			System.arraycopy(getBytesNullable(valuesWithNulls.get(i)), 0, out, i*8, 8);
		}
		return out;
	}
	
	public static class Tests{
		@Test
		public void testBytes1(){
			double a = 12354234.456d;
			byte[] abytes = getBytes(a);
			double aback = fromBytes(abytes, 0);
			Assert.assertTrue(a==aback);
			
			double b = -1234568.456d;
			byte[] bbytes = getBytes(b);
			double bback = fromBytes(bbytes, 0);
			Assert.assertTrue(b==bback);
			
			Assert.assertTrue(ByteTool.bitwiseCompare(abytes, bbytes) < 0);//positives and negatives are reversed
		}
		
		@Test
		public void testToFromByteArray(){
			double one = 2.39483;
			double two = -583.2039;
			double three = 5;
			double four = -.0000001;
			
			List<Double> doubles = ListTool.create();
			doubles.add(one);
			doubles.add(two);
			doubles.add(null);
			doubles.add(three);
			doubles.add(four);
			
			byte[] doubleBytes = getDoubleByteArray(doubles);
			List<Double> result = fromDoubleByteArray(doubleBytes, 0);
			Assert.assertEquals(doubles, result);
			
		}
	}
}
