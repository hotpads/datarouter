package com.hotpads.util.core.bytes;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.util.core.DrByteTool;

public class FloatByteTool{

	public static byte[] getBytes(float in){
		int bits = Float.floatToIntBits(in);
		byte[] out = new byte[4];
		out[0] = (byte) (bits >>> 24);
		out[1] = (byte) (bits >>> 16);
		out[2] = (byte) (bits >>> 8);
		out[3] = (byte) bits;
		return out;
	}

	public static float fromBytes(final byte[] bytes, final int startIdx){
		int bits = 
		  ((bytes[startIdx    ] & 0xff) << 24)
		| ((bytes[startIdx + 1] & 0xff) << 16)
		| ((bytes[startIdx + 2] & 0xff) <<  8)
		|  (bytes[startIdx + 3] & 0xff);
		return Float.intBitsToFloat(bits);
	}
	
	
	
	public static class Tests{
		@Test
		public void testBytes1(){
			float a = 123.456f;
			byte[] abytes = getBytes(a);
			float aback = fromBytes(abytes, 0);
			Assert.assertTrue(a==aback);
			
			float b = -123.456f;
			byte[] bbytes = getBytes(b);
			float bback = fromBytes(bbytes, 0);
			Assert.assertTrue(b==bback);
			
			Assert.assertTrue(DrByteTool.bitwiseCompare(abytes, bbytes) < 0);//positives and negatives are reversed
		}
	}
}
