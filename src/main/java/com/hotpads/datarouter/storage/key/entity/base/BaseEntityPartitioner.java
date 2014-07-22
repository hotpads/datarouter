package com.hotpads.datarouter.storage.key.entity.base;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.util.core.bytes.IntegerByteTool;

public abstract class BaseEntityPartitioner<EK extends EntityKey<EK>>
implements EntityPartitioner<EK>{

	private static final int
		MIN_PARTITIONS = 1,
		MAX_ONE_BYTE_NUM_PARTITIONS = 256,
		MAX_PARTITIONS = 1 << 16;
		
	@Override
	public int getNumPrefixBytes(){
		return getNumPrefixBytesStatic(getNumPartitions());
	}
	
	@Override
	//TODO skip intermediate array
	public byte[] getPrefix(EK ek){
		int partition = getPartition(ek);
		byte[] fourBytePrefix = IntegerByteTool.getComparableBytes(partition);
		int numPrefixBytes = getNumPrefixBytes();
		byte[] prefix = new byte[numPrefixBytes];
		int offset = 4 - numPrefixBytes;
		System.arraycopy(fourBytePrefix, offset, prefix, 0, numPrefixBytes);
		return prefix;
	}
	
	/*********** static for testing *************/
	
	private static int getNumPrefixBytesStatic(int numPartitions){
		if(numPartitions < MIN_PARTITIONS){
			throw new IllegalArgumentException("must have at least one partition");
		}
		if(numPartitions == 1){ return 0; }
		if(numPartitions <= MAX_ONE_BYTE_NUM_PARTITIONS){ return 1; }
		if(numPartitions <= MAX_PARTITIONS){ return 2; }
		throw new IllegalArgumentException("max partitions is "+MAX_PARTITIONS);
	}
	
	
	public static class BaseEntityPartitionerTests{
		@Test(expected=IndexOutOfBoundsException.class)
		public void testMinBound(){
			getNumPrefixBytesStatic(0);
		}
		@Test
		public void testValidNumPartitions(){
			Assert.assertEquals(0, getNumPrefixBytesStatic(1));
			Assert.assertEquals(1, getNumPrefixBytesStatic(2));
			Assert.assertEquals(1, getNumPrefixBytesStatic(255));
			Assert.assertEquals(1, getNumPrefixBytesStatic(256));
			Assert.assertEquals(2, getNumPrefixBytesStatic(257));
			Assert.assertEquals(2, getNumPrefixBytesStatic(65535));
			Assert.assertEquals(2, getNumPrefixBytesStatic(65536));
		}
		@Test(expected=IndexOutOfBoundsException.class)
		public void testMaxBound(){
			getNumPrefixBytesStatic(65537);
		}
	}
	
}
