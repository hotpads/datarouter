package com.hotpads.util.core.number;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.ArrayTool;
import com.hotpads.datarouter.util.core.ByteTool;

public class VarLong{
	
	public static final byte 
		BYTE_7_RIGHT_BITS_SET = 127,
		BYTE_LEFT_BIT_SET = -128;
	
	public static final long 
		LONG_7_RIGHT_BITS_SET = 127,
		LONG_8TH_BIT_SET = 128;
	
	public static final byte[] MAX_VALUE_BYTES = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, 127};
	
	protected long value;
	
	public VarLong(long value){
		set(value);
	}
	
	public VarLong(byte[] bytes){
		set(bytes);
	}
	
	public VarLong(InputStream is) throws IOException{
		set(is);
	}
	
	public void set(long value){
		if(value < 0){ throw new IllegalArgumentException("must be postitive long"); }
		this.value = value;
	}
	
	public void set(byte[] bytes){
		if(ArrayTool.isEmpty(bytes) || bytes.length > 9){
			throw new IllegalArgumentException("invalid bytes "+ByteTool.getBinaryStringBigEndian(bytes));
		}
		value = 0;
		for(int i=0; i < bytes.length; ++i){
			byte b = bytes[i];
			long shifted = BYTE_7_RIGHT_BITS_SET & bytes[i];//kill leftmost bit
			shifted <<= 7*i;
			value |= shifted;
			if(b >= 0){ break; }//first bit was 0, so that's the last byte in the VarLong
		}
	}
	
	public void set(InputStream is) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while(true){
			int b = is.read();
			//FieldSetTool relies on this IllegalArgumentException to know it's hit the end of a databean
			if(b==-1){ throw new IllegalArgumentException("end of InputStream"); }//unexpectedly hit the end of the input stream
			baos.write(b);
			if(b < 128){ break; }
		}
		set(baos.toByteArray());
	}
	
	public long getValue(){
		return value;
	}
	
	public int getValueInt(){
		return (int)value;
	}
	
	public byte[] getBytes(){
		int numBytes = numBytes(value);
		byte[] bytes = new byte[numBytes];
		long remainder = value;
		for(int i=0; i < numBytes-1; ++i){
			bytes[i] = (byte)((remainder & LONG_7_RIGHT_BITS_SET) | LONG_8TH_BIT_SET);//set the left bit
			remainder >>= 7;
		}
		bytes[numBytes-1] = (byte)(remainder & LONG_7_RIGHT_BITS_SET);//do not set the left bit
		return bytes;
	}
	
	public int getNumBytes(){
		return numBytes(value);
	}
	
	protected static int numBytes(long in){//do a check for illegal arguments if not protected
		if(in==0){ return 1; }//doesn't work with the formula below
		return (70 - Long.numberOfLeadingZeros(in)) / 7;//70 comes from 64+(7-1)
	}
	
	public static class VarLongTests{
		@Test public void testNumBytes(){
			Assert.assertEquals(1, numBytes(0));
			Assert.assertEquals(1, numBytes(1));
			Assert.assertEquals(1, numBytes(100));
			Assert.assertEquals(1, numBytes(126));
			Assert.assertEquals(1, numBytes(127));
			Assert.assertEquals(2, numBytes(128));
			Assert.assertEquals(2, numBytes(129));
			Assert.assertEquals(9, numBytes(Long.MAX_VALUE));
		}
		
		@Test public void testToBytes(){
			VarLong v0 = new VarLong(0);
			Assert.assertArrayEquals(new byte[]{0}, v0.getBytes());
			VarLong v1 = new VarLong(1);
			Assert.assertArrayEquals(new byte[]{1}, v1.getBytes());
			VarLong v63 = new VarLong(63);
			Assert.assertArrayEquals(new byte[]{63}, v63.getBytes());
			VarLong v127 = new VarLong(127);
			Assert.assertArrayEquals(new byte[]{127}, v127.getBytes());
			VarLong v128 = new VarLong(128);
			Assert.assertArrayEquals(new byte[]{-128, 1}, v128.getBytes());
			VarLong v155 = new VarLong(155);
			Assert.assertArrayEquals(new byte[]{-128+27, 1}, v155.getBytes());
			VarLong vMax = new VarLong(Long.MAX_VALUE);
			Assert.assertArrayEquals(MAX_VALUE_BYTES, vMax.getBytes());
		}
		@Test public void testFromBytes(){
			VarLong vMax = new VarLong(MAX_VALUE_BYTES);
			Assert.assertEquals(Long.MAX_VALUE, vMax.getValue());
		}
		@Test public void testRoundTrips(){
			Random random = new Random();
			for(int i=0; i < 10000; ++i){
				long value = RandomTool.nextPositiveLong(random);
				byte[] bytes = new VarLong(value).getBytes();
				long roundTripped = new VarLong(bytes).getValue();
				Assert.assertEquals(value, roundTripped);
			}
		}
		@Test public void testEmptyInputStream() throws IOException{
			ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
			int numExceptions = 0;
			try{
				VarLong v1 = new VarLong(is);
			}catch(IllegalArgumentException iae){
				++numExceptions;
			}
			Assert.assertEquals(1, numExceptions);
		}
		@Test public void testInputStreams() throws IOException{
			ByteArrayInputStream is;
			is = new ByteArrayInputStream(new byte[]{0});
			VarLong v0 = new VarLong(is);
			Assert.assertEquals(0, v0.getValue());
			is = new ByteArrayInputStream(new byte[]{5});
			VarLong v5 = new VarLong(is);
			Assert.assertEquals(5, v5.getValue());
			is = new ByteArrayInputStream(new byte[]{-128+27, 1});
			VarLong v155 = new VarLong(is);
			Assert.assertEquals(155, v155.getValue());
		}
	}
	
}
