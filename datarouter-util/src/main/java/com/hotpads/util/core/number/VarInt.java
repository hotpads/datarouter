package com.hotpads.util.core.number;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrByteTool;

public class VarInt{

	private static final byte BYTE_7_RIGHT_BITS_SET = 127;

	private static final long
		INT_3_RIGHT_BITS_SET = 127,
		INT_8TH_BIT_SET = 128;

	private int value;

	public VarInt(int value){
		if(value < 0){
			throw new IllegalArgumentException("must be postitive int");
		}
		this.value = value;
	}

	public VarInt(byte[] bytes, int offset){
		set(bytes, offset);
	}

	public VarInt(byte[] bytes){
		this(bytes, 0);
	}

	private VarInt(InputStream is) throws IOException{
		set(is);
	}

	private VarInt(ReadableByteChannel channel)throws IOException{
		set(channel);
	}

	private void set(byte[] bytes, int offset){
		if(offset >= bytes.length){
			//TODO check other invalidity conditions
			throw new IllegalArgumentException("invalid bytes " + DrByteTool.getBinaryStringBigEndian(bytes));
		}
		value = 0;
		for(int i = 0;; ++i){
			byte byteVar = bytes[offset + i];
			int shifted = BYTE_7_RIGHT_BITS_SET & byteVar;//kill leftmost bit
			shifted <<= 7 * i;
			value |= shifted;
			if(byteVar >= 0){//first bit was 0, so that's the last byte in the VarLong
				break;
			}
		}
	}

	private void set(InputStream is) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while(true){
			int byteVar = is.read();
			//FieldSetTool relies on this IllegalArgumentException to know it's hit the end of a databean
			if(byteVar == -1){// unexpectedly hit the end of the input stream
				throw new IllegalArgumentException("end of InputStream");
			}
			baos.write(byteVar);
			if(byteVar < 128){
				break;
			}
		}
		set(baos.toByteArray(), 0);
	}

	private void set(ReadableByteChannel fs) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteBuffer byteBuffer = ByteBuffer.allocate(1);
		while(true){
			byteBuffer.clear();
			if(fs.read(byteBuffer) == -1){// unexpectedly hit the end of the input stream
				throw new IllegalArgumentException("end of InputStream");
			}
			int byteVar = byteBuffer.get(0);
			baos.write(byteVar);
			if(byteVar < 128){
				break;
			}
		}
		set(baos.toByteArray(), 0);
	}
	public int getValue(){
		return value;
	}

	public byte[] getBytes(){
		int numBytes = numBytes(value);
		byte[] bytes = new byte[numBytes];
		int remainder = value;
		for(int i = 0; i < numBytes - 1; ++i){
			bytes[i] = (byte)(remainder & INT_3_RIGHT_BITS_SET | INT_8TH_BIT_SET);//set the left bit
			remainder >>= 7;
		}
		bytes[numBytes - 1] = (byte)(remainder & INT_3_RIGHT_BITS_SET);// do not set the left bit
		return bytes;
	}

	public int getNumBytes(){
		return numBytes(value);
	}

	public static int numBytes(int in){//do a check for illegal arguments if not protected
		if(in == 0){// doesn't work with the formula below
			return 1;
		}
		return (38 - Integer.numberOfLeadingZeros(in)) / 7;//38 comes from 32+(7-1)
	}

	public static class VarLongTests{

		private static final byte[] MAX_VALUE_BYTES = new byte[]{-1, -1, -1, -1, 7};

		@Test
		public void testOffset(){
			Assert.assertEquals(28, new VarInt(new byte[]{-1,-1,28}, 2).value);
		}

		@Test
		public void testNumBytes(){
			Assert.assertEquals(1, numBytes(0));
			Assert.assertEquals(1, numBytes(1));
			Assert.assertEquals(1, numBytes(100));
			Assert.assertEquals(1, numBytes(126));
			Assert.assertEquals(1, numBytes(127));
			Assert.assertEquals(2, numBytes(128));
			Assert.assertEquals(2, numBytes(129));
			Assert.assertEquals(5, numBytes(Integer.MAX_VALUE));
		}

		@Test
		public void testToBytes(){
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
			Assert.assertArrayEquals(new byte[]{-128 + 27, 1}, v155.getBytes());
			VarInt max = new VarInt(Integer.MAX_VALUE);
			Assert.assertArrayEquals(MAX_VALUE_BYTES, max.getBytes());
		}

		@Test
		public void testFromBytes(){
			VarInt max = new VarInt(MAX_VALUE_BYTES);
			Assert.assertEquals(Integer.MAX_VALUE, max.getValue());
		}

		@Test
		public void testRoundTrips(){
			Random random = new Random();
			for(int i = 0; i < 10000; ++i){
				int value = RandomTool.nextPositiveInt(random);
				byte[] bytes = new VarInt(value).getBytes();
				int roundTripped = new VarInt(bytes).getValue();
				Assert.assertEquals(value, roundTripped);
			}
		}

		@Test
		public void testEmptyInputStream() throws IOException{
			ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
			int numExceptions = 0;
			try{
				new VarInt(is);
			}catch(IllegalArgumentException iae){
				++numExceptions;
			}
			Assert.assertEquals(1, numExceptions);
		}

		@Test
		public void testInputStreams() throws IOException{
			ByteArrayInputStream is;
			is = new ByteArrayInputStream(new byte[]{0});
			VarInt v0 = new VarInt(is);
			Assert.assertEquals(0, v0.getValue());
			is = new ByteArrayInputStream(new byte[]{5});
			VarInt v5 = new VarInt(is);
			Assert.assertEquals(5, v5.getValue());
			is = new ByteArrayInputStream(new byte[]{-128 + 27, 1});
			VarInt v155 = new VarInt(is);
			Assert.assertEquals(155, v155.getValue());
			is = new ByteArrayInputStream(new byte[]{-5, 24});
			VarInt v3195 = new VarInt(is);
			Assert.assertEquals(3195, v3195.getValue());
		}

		@Test
		public void testEmptyFileChannel() throws IOException{
			ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
			int numExceptions = 0;
			try{
				new VarInt(is);
			}catch(IllegalArgumentException iae){
				++numExceptions;
			}
			Assert.assertEquals(1, numExceptions);
		}

		@Test
		public void testFileChannels() throws IOException{
			ByteArrayInputStream is;
			is = new ByteArrayInputStream(new byte[]{0});
			ReadableByteChannel channel = Channels.newChannel(is);
			VarInt v0 = new VarInt(channel);
			Assert.assertEquals(0, v0.getValue());
		}
	}

}
