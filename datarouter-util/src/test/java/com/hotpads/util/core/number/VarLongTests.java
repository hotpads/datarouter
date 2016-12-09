package com.hotpads.util.core.number;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

import org.junit.Assert;
import org.testng.annotations.Test;


public class VarLongTests{

	private static final byte[] LONG_MAX_VALUE_BYTES = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, 127};
	private static final byte[] INT_MAX_VALUE_BYTES = new byte[]{-1, -1, -1, -1, 7};

	@Test
	public void testNumBytes(){
		Assert.assertEquals(1, new VarLong(0).getNumBytes());
		Assert.assertEquals(1, new VarLong(1).getNumBytes());
		Assert.assertEquals(1, new VarLong(100).getNumBytes());
		Assert.assertEquals(1, new VarLong(126).getNumBytes());
		Assert.assertEquals(1, new VarLong(127).getNumBytes());
		Assert.assertEquals(2, new VarLong(128).getNumBytes());
		Assert.assertEquals(2, new VarLong(129).getNumBytes());
		Assert.assertEquals(5, new VarLong(Integer.MAX_VALUE).getNumBytes());
		Assert.assertEquals(9, new VarLong(Long.MAX_VALUE).getNumBytes());
	}

	@Test
	public void testToBytes(){
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
		Assert.assertArrayEquals(new byte[]{-128 + 27, 1}, v155.getBytes());
		VarLong maxLong = new VarLong(Long.MAX_VALUE);
		Assert.assertArrayEquals(LONG_MAX_VALUE_BYTES, maxLong.getBytes());
		VarLong maxInt = new VarLong(Integer.MAX_VALUE);
		Assert.assertArrayEquals(INT_MAX_VALUE_BYTES, maxInt.getBytes());
	}

	@Test
	public void testFromBytes(){
		VarLong maxLong = VarLong.fromByteArray(LONG_MAX_VALUE_BYTES);
		Assert.assertEquals(Long.MAX_VALUE, maxLong.getValue());
		VarLong maxInt = VarLong.fromByteArray(INT_MAX_VALUE_BYTES);
		Assert.assertEquals(Integer.MAX_VALUE, maxInt.getValue());
	}

	@Test
	public void testRoundTrips(){
		Random random = new Random();
		for(int i = 0; i < 10000; ++i){
			long value = RandomTool.nextPositiveLong(random);
			byte[] bytes = new VarLong(value).getBytes();
			long roundTripped = VarLong.fromByteArray(bytes).getValue();
			Assert.assertEquals(value, roundTripped);
		}
	}

	@Test
	public void testInputStreams() throws IOException{
		ByteArrayInputStream is;
		is = new ByteArrayInputStream(new byte[]{0});
		VarLong v0 = VarLong.fromInputStream(is);
		Assert.assertEquals(0, v0.getValue());
		is = new ByteArrayInputStream(new byte[]{5});
		VarLong v5 = VarLong.fromInputStream(is);
		Assert.assertEquals(5, v5.getValue());
		is = new ByteArrayInputStream(new byte[]{-128 + 27, 1});
		VarLong v155 = VarLong.fromInputStream(is);
		Assert.assertEquals(155, v155.getValue());
		is = new ByteArrayInputStream(new byte[]{-5, 24});
		VarLong v3195 = VarLong.fromInputStream(is);
		Assert.assertEquals(3195, v3195.getValue());
	}

	@Test
	public void testOffset(){
		Assert.assertEquals(28, VarLong.fromByteArray(new byte[]{-1,-1,28}, 2).getValue());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidOffset(){
		VarLong.fromByteArray(new byte[]{0,0,0}, 4);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEmptyInputStream() throws IOException{
		VarLong.fromInputStream(new ByteArrayInputStream(new byte[0]));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEmptyFileChannel() throws IOException{
		VarLong.fromInputStream(new ByteArrayInputStream(new byte[0]));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeInteger(){
		new VarLong(-1);
	}

	@Test
	public void testFileChannels() throws IOException{
		ByteArrayInputStream is;
		is = new ByteArrayInputStream(new byte[]{0});
		ReadableByteChannel channel = Channels.newChannel(is);
		VarLong v0 = VarLong.fromReadableByteChannel(channel);
		Assert.assertEquals(0, v0.getValue());
	}

}