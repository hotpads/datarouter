package io.datarouter.util.number;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.number.RandomTool;
import io.datarouter.util.varint.VarLong;


public class VarLongTests{

	private static final byte[] LONG_MAX_VALUE_BYTES = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, 127};
	private static final byte[] INT_MAX_VALUE_BYTES = new byte[]{-1, -1, -1, -1, 7};

	@Test
	public void testNumBytes(){
		Assert.assertEquals(new VarLong(0).getNumBytes(), 1);
		Assert.assertEquals(new VarLong(1).getNumBytes(), 1);
		Assert.assertEquals(new VarLong(100).getNumBytes(), 1);
		Assert.assertEquals(new VarLong(126).getNumBytes(), 1);
		Assert.assertEquals(new VarLong(127).getNumBytes(), 1);
		Assert.assertEquals(new VarLong(128).getNumBytes(), 2);
		Assert.assertEquals(new VarLong(129).getNumBytes(), 2);
		Assert.assertEquals(new VarLong(Integer.MAX_VALUE).getNumBytes(), 5);
		Assert.assertEquals(new VarLong(Long.MAX_VALUE).getNumBytes(), 9);
	}

	@Test
	public void testToBytes(){
		VarLong v0 = new VarLong(0);
		Assert.assertEquals(v0.getBytes(), new byte[]{0});
		VarLong v1 = new VarLong(1);
		Assert.assertEquals(v1.getBytes(), new byte[]{1});
		VarLong v63 = new VarLong(63);
		Assert.assertEquals(v63.getBytes(), new byte[]{63});
		VarLong v127 = new VarLong(127);
		Assert.assertEquals(v127.getBytes(), new byte[]{127});
		VarLong v128 = new VarLong(128);
		Assert.assertEquals(v128.getBytes(), new byte[]{-128, 1});
		VarLong v155 = new VarLong(155);
		Assert.assertEquals(v155.getBytes(), new byte[]{-128 + 27, 1});
		VarLong maxLong = new VarLong(Long.MAX_VALUE);
		Assert.assertEquals(maxLong.getBytes(), LONG_MAX_VALUE_BYTES);
		VarLong maxInt = new VarLong(Integer.MAX_VALUE);
		Assert.assertEquals(maxInt.getBytes(), INT_MAX_VALUE_BYTES);
	}

	@Test
	public void testFromBytes(){
		VarLong maxLong = VarLong.fromByteArray(LONG_MAX_VALUE_BYTES);
		Assert.assertEquals(maxLong.getValue(), Long.MAX_VALUE);
		VarLong maxInt = VarLong.fromByteArray(INT_MAX_VALUE_BYTES);
		Assert.assertEquals(maxInt.getValue(), Integer.MAX_VALUE);
	}

	@Test
	public void testRoundTrips(){
		Random random = new Random();
		for(int i = 0; i < 10000; ++i){
			long value = RandomTool.nextPositiveLong(random);
			byte[] bytes = new VarLong(value).getBytes();
			long roundTripped = VarLong.fromByteArray(bytes).getValue();
			Assert.assertEquals(roundTripped, value);
		}
	}

	@Test
	public void testInputStreams() throws IOException{
		ByteArrayInputStream is;
		is = new ByteArrayInputStream(new byte[]{0});
		VarLong v0 = VarLong.fromInputStream(is);
		Assert.assertEquals(v0.getValue(), 0);
		is = new ByteArrayInputStream(new byte[]{5});
		VarLong v5 = VarLong.fromInputStream(is);
		Assert.assertEquals(v5.getValue(), 5);
		is = new ByteArrayInputStream(new byte[]{-128 + 27, 1});
		VarLong v155 = VarLong.fromInputStream(is);
		Assert.assertEquals(v155.getValue(), 155);
		is = new ByteArrayInputStream(new byte[]{-5, 24});
		VarLong v3195 = VarLong.fromInputStream(is);
		Assert.assertEquals(v3195.getValue(), 3195);
	}

	@Test
	public void testOffset(){
		Assert.assertEquals(VarLong.fromByteArray(new byte[]{-1,-1,28}, 2).getValue(), 28);
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
		ByteArrayInputStream is = new ByteArrayInputStream(new byte[]{0});
		ReadableByteChannel channel = Channels.newChannel(is);
		VarLong v0 = VarLong.fromReadableByteChannel(channel);
		Assert.assertEquals(v0.getValue(), 0);
	}

}