package com.hotpads.util.core.number;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.ByteTool;

public class VarInt{
	
	public static final byte 
		BYTE_7_RIGHT_BITS_SET = 127,
		BYTE_LEFT_BIT_SET = -128;
	
	public static final long 
		INT_3_RIGHT_BITS_SET = 127,
		INT_8TH_BIT_SET = 128;
	
	public static final byte[] MAX_VALUE_BYTES = new byte[]{-1, -1, -1, -1, 7};
	public static final int MAX_BYTES = 5;
	
	protected int value;
	
	public VarInt(int value){
		set(value);
	}
	
	public VarInt(byte[] bytes, int offset){
		set(bytes, offset);
	}
	
	public VarInt(byte[] bytes){
		set(bytes, 0);
	}
	
	public VarInt(InputStream is) throws IOException{
		set(is);
	}
	
	public VarInt(ReadableByteChannel channel)throws IOException{
		set(channel);
	}
	
	public void set(int value){
		if(value < 0){ throw new IllegalArgumentException("must be postitive int"); }
		this.value = value;
	}
	
	public void set(byte[] bytes){
		set(bytes, 0);
	}
	
	public void set(byte[] bytes, int offset){
		if(offset >= bytes.length){
			//TODO check other invalidity conditions
			throw new IllegalArgumentException("invalid bytes "+ByteTool.getBinaryStringBigEndian(bytes));
		}
		value = 0;
		for(int i=0; ; ++i){
			byte b = bytes[offset + i];
			int shifted = BYTE_7_RIGHT_BITS_SET & b;//kill leftmost bit
			shifted <<= 7 * i;
			value |= shifted;
			if(b >= 0){ break; }//first bit was 0, so that's the last byte in the VarLong
		}
	}
	
	//should be faster than the above, but not correct yet
//	public void set(byte[] bytes, int offset){
//		if(ArrayTool.isEmpty(bytes) || bytes.length > 5){
//			//TODO check other invalidity conditions
//			throw new IllegalArgumentException("invalid bytes "+ByteTool.getBinaryStringBigEndian(bytes));
//		}
//		value = BYTE_7_RIGHT_BITS_SET & bytes[offset];
//		int i = offset;
//		while(bytes[++i] < 0){
//			value <<= 7;
//			value |= BYTE_7_RIGHT_BITS_SET & bytes[i];
//		}
//	}
	
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
	
	public void set(ReadableByteChannel fs) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteBuffer byteBuffer = ByteBuffer.allocate(1);
		while(true){
			byteBuffer.clear();
			if(fs.read(byteBuffer) == -1){ throw new IllegalArgumentException("end of InputStream"); }//unexpectedly hit the end of the input stream
			int b = byteBuffer.get(0);
			baos.write(b);
			if(b < 128){ break; }
		}
		set(baos.toByteArray());
	}
	public int getValue(){
		return value;
	}
	
	public byte[] getBytes(){
		int numBytes = numBytes(value);
		byte[] bytes = new byte[numBytes];
		int remainder = value;
		for(int i=0; i < numBytes-1; ++i){
			bytes[i] = (byte)((remainder & INT_3_RIGHT_BITS_SET) | INT_8TH_BIT_SET);//set the left bit
			remainder >>= 7;
		}
		bytes[numBytes-1] = (byte)(remainder & INT_3_RIGHT_BITS_SET);//do not set the left bit
		return bytes;
	}
	
	public int getNumBytes(){
		return numBytes(value);
	}
	
	public static int numBytes(int in){//do a check for illegal arguments if not protected
		if(in==0){ return 1; }//doesn't work with the formula below
		return (38 - Integer.numberOfLeadingZeros(in)) / 7;//38 comes from 32+(7-1)
	}
	
	public static class VarLongTests{
		@Test public void testOffset(){
			Assert.assertEquals(28, new VarInt(new byte[]{-1,-1,28}, 2).value);
		}
		@Test public void testNumBytes(){
			Assert.assertEquals(1, numBytes(0));
			Assert.assertEquals(1, numBytes(1));
			Assert.assertEquals(1, numBytes(100));
			Assert.assertEquals(1, numBytes(126));
			Assert.assertEquals(1, numBytes(127));
			Assert.assertEquals(2, numBytes(128));
			Assert.assertEquals(2, numBytes(129));
			Assert.assertEquals(5, numBytes(Integer.MAX_VALUE));
		}
		
		@Test public void testToBytes(){
			VarInt v0 = new VarInt(0);
			Assert.assertArrayEquals(new byte[]{0}, v0.getBytes());
			VarInt v1 = new VarInt(1);
			Assert.assertArrayEquals(new byte[]{1}, v1.getBytes());
			VarInt v63 = new VarInt(63);
			Assert.assertArrayEquals(new byte[]{63}, v63.getBytes());
			VarInt v127 = new VarInt(127);
			Assert.assertArrayEquals(new byte[]{127}, v127.getBytes());
			VarInt v128 = new VarInt(128);
			Assert.assertArrayEquals(new byte[]{-128, 1}, v128.getBytes());
			VarInt v155 = new VarInt(155);
			Assert.assertArrayEquals(new byte[]{-128+27, 1}, v155.getBytes());
			VarInt vMax = new VarInt(Integer.MAX_VALUE);
			Assert.assertArrayEquals(MAX_VALUE_BYTES, vMax.getBytes());
		}
		@Test public void testFromBytes(){
			VarInt vMax = new VarInt(MAX_VALUE_BYTES);
			Assert.assertEquals(Integer.MAX_VALUE, vMax.getValue());
		}
		@Test public void testRoundTrips(){
			Random random = new Random();
			for(int i=0; i < 10000; ++i){
				int value = RandomTool.nextPositiveInt(random);
				byte[] bytes = new VarInt(value).getBytes();
				int roundTripped = new VarInt(bytes).getValue();
				Assert.assertEquals(value, roundTripped);
			}
		}
		@Test public void testEmptyInputStream() throws IOException{
			ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
			int numExceptions = 0;
			try{
				VarInt v1 = new VarInt(is);
			}catch(IllegalArgumentException iae){
				++numExceptions;
			}
			Assert.assertEquals(1, numExceptions);
		}
		@Test public void testInputStreams() throws IOException{
			ByteArrayInputStream is;
			is = new ByteArrayInputStream(new byte[]{0});
			VarInt v0 = new VarInt(is);
			Assert.assertEquals(0, v0.getValue());
			is = new ByteArrayInputStream(new byte[]{5});
			VarInt v5 = new VarInt(is);
			Assert.assertEquals(5, v5.getValue());
			is = new ByteArrayInputStream(new byte[]{-128+27, 1});
			VarInt v155 = new VarInt(is);
			Assert.assertEquals(155, v155.getValue());
			is = new ByteArrayInputStream(new byte[]{-5, 24});
			VarInt v3195 = new VarInt(is);
			Assert.assertEquals(3195, v3195.getValue());
		}
		@Test public void testEmptyFileChannel() throws IOException{
			ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
			int numExceptions = 0;
			try{
				VarInt v1 = new VarInt(is);
			}catch(IllegalArgumentException iae){
				++numExceptions;
			}
			Assert.assertEquals(1, numExceptions);
		}
		@Test public void testFileChannels() throws IOException{
			ByteArrayInputStream is;
			is = new ByteArrayInputStream(new byte[]{0});
			ReadableByteChannel channel = Channels.newChannel(is);
			VarInt v0 = new VarInt(channel);
			Assert.assertEquals(0, v0.getValue());
			
			//repeat channel test with these values
//			is = new ByteArrayInputStream(new byte[]{0});
//			VarInt v0 = new VarInt(is);
//			Assert.assertEquals(0, v0.getValue());
//			is = new ByteArrayInputStream(new byte[]{5});
//			VarInt v5 = new VarInt(is);
//			Assert.assertEquals(5, v5.getValue());
//			is = new ByteArrayInputStream(new byte[]{-128+27, 1});
//			VarInt v155 = new VarInt(is);
//			Assert.assertEquals(155, v155.getValue());
//			is = new ByteArrayInputStream(new byte[]{-5, 24});
//			VarInt v3195 = new VarInt(is);
//			Assert.assertEquals(3195, v3195.getValue());
		}
	}
	
}
