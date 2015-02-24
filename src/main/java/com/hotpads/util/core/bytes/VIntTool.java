package com.hotpads.util.core.bytes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.io.OutputStreamTool;
import com.hotpads.util.core.number.RandomTool;

public class VIntTool{

	public static final byte BYTE_7_RIGHT_BITS_SET = 127, BYTE_LEFT_BIT_SET = -128;

	public static final long INT_7_RIGHT_BITS_SET = 127, INT_8TH_BIT_SET = 128;

	public static final byte[] MAX_VALUE_BYTES = new byte[]{-1, -1, -1, -1, 7};

	
	/********************* int -> bytes **************************/

	public static int numBytes(int in){
		if(in == 0){
			// doesn't work with the formula below
			return 1;
		}
		return (38 - Integer.numberOfLeadingZeros(in)) / 7;// 38 comes from 32+(7-1)
	}

	public static byte[] getBytes(int value){
		int numBytes = numBytes(value);
		byte[] bytes = new byte[numBytes];
		int remainder = value;
		for(int i = 0; i < numBytes - 1; ++i){
			// set the left bit
			bytes[i] = (byte)((remainder & INT_7_RIGHT_BITS_SET) | INT_8TH_BIT_SET);
			remainder >>= 7;
		}
		// do not set the left bit
		bytes[numBytes - 1] = (byte)(remainder & INT_7_RIGHT_BITS_SET);
		return bytes;
	}

	public static int writeBytes(int value, OutputStream os){
		int numBytes = numBytes(value);
		int remainder = value;
		for(int i = 0; i < numBytes - 1; ++i){
			// set the left bit
			OutputStreamTool.write(os, (byte)((remainder & INT_7_RIGHT_BITS_SET) | INT_8TH_BIT_SET));
			remainder >>= 7;
		}
		// do not set the left bit
		OutputStreamTool.write(os, (byte)(remainder & INT_7_RIGHT_BITS_SET));
		return numBytes;
	}
	

	/******************** bytes -> int **************************/

	public static int getInt(byte[] bytes){
		return getInt(bytes, 0);
	}

	public static int getInt(byte[] bytes, int offset){
		int value = 0;
		for(int i = 0;; ++i){
			byte b = bytes[offset + i];
			int shifted = BYTE_7_RIGHT_BITS_SET & b;// kill leftmost bit
			shifted <<= 7 * i;
			value |= shifted;
			if(b >= 0){
				break;
			}
		}
		return value;
	}

	public static int getInt(InputStream is) throws IOException{
		int value = 0;
		int i = 0;
		int b;
		do{
			b = is.read();
			int shifted = BYTE_7_RIGHT_BITS_SET & b;// kill leftmost bit
			shifted <<= 7 * i;
			value |= shifted;
			++i;
		}while(b > Byte.MAX_VALUE);
		return value;
	}
	
	/************************** Tests ********************************/

	public static class TestVIntTool {
	  @Test
	  public void testNumBytes() {
	    Assert.assertEquals(1, VIntTool.numBytes(0));
	    Assert.assertEquals(1, VIntTool.numBytes(1));
	    Assert.assertEquals(1, VIntTool.numBytes(100));
	    Assert.assertEquals(1, VIntTool.numBytes(126));
	    Assert.assertEquals(1, VIntTool.numBytes(127));
	    Assert.assertEquals(2, VIntTool.numBytes(128));
	    Assert.assertEquals(2, VIntTool.numBytes(129));
	    Assert.assertEquals(5, VIntTool.numBytes(Integer.MAX_VALUE));
	  }

	  @Test
	  public void testWriteBytes() {
	    Assert.assertArrayEquals(new byte[] { 0 }, bytesViaOutputStream(0));
	    Assert.assertArrayEquals(new byte[] { 1 }, bytesViaOutputStream(1));
	    Assert.assertArrayEquals(new byte[] { 63 }, bytesViaOutputStream(63));
	    Assert.assertArrayEquals(new byte[] { 127 }, bytesViaOutputStream(127));
	    Assert.assertArrayEquals(new byte[] { -128, 1 }, bytesViaOutputStream(128));
	    Assert.assertArrayEquals(new byte[] { -128 + 27, 1 }, bytesViaOutputStream(155));
	    Assert.assertArrayEquals(VIntTool.MAX_VALUE_BYTES, bytesViaOutputStream(Integer.MAX_VALUE));
	  }

	  private byte[] bytesViaOutputStream(int value) {
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    VIntTool.writeBytes(value, os);
	    return os.toByteArray();
	  }

	  @Test
	  public void testToBytes() {
	    Assert.assertArrayEquals(new byte[] { 0 }, VIntTool.getBytes(0));
	    Assert.assertArrayEquals(new byte[] { 1 }, VIntTool.getBytes(1));
	    Assert.assertArrayEquals(new byte[] { 63 }, VIntTool.getBytes(63));
	    Assert.assertArrayEquals(new byte[] { 127 }, VIntTool.getBytes(127));
	    Assert.assertArrayEquals(new byte[] { -128, 1 }, VIntTool.getBytes(128));
	    Assert.assertArrayEquals(new byte[] { -128 + 27, 1 }, VIntTool.getBytes(155));
	    Assert.assertArrayEquals(VIntTool.MAX_VALUE_BYTES, VIntTool.getBytes(Integer.MAX_VALUE));
	  }

	  @Test
	  public void testFromBytes() {
	    Assert.assertEquals(Integer.MAX_VALUE, VIntTool.getInt(VIntTool.MAX_VALUE_BYTES));
	  }

	  @Test
	  public void testRoundTrips() {
	    Random random = new Random();
	    for (int i = 0; i < 10000; ++i) {
	      int value = RandomTool.nextPositiveInt(random);
	      byte[] bytes = VIntTool.getBytes(value);
	      int roundTripped = VIntTool.getInt(bytes);
	      Assert.assertEquals(value, roundTripped);
	    }
	  }

	  @Test
	  public void testInputStreams() throws IOException {
	    ByteArrayInputStream is;
	    is = new ByteArrayInputStream(new byte[] { 0 });
	    Assert.assertEquals(0, VIntTool.getInt(is));
	    is = new ByteArrayInputStream(new byte[] { 5 });
	    Assert.assertEquals(5, VIntTool.getInt(is));
	    is = new ByteArrayInputStream(new byte[] { -128 + 27, 1 });
	    Assert.assertEquals(155, VIntTool.getInt(is));
	  }
	}
}