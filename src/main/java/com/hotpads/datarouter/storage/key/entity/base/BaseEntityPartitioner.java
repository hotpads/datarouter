package com.hotpads.datarouter.storage.key.entity.base;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.util.core.bytes.IntegerByteTool;

public abstract class BaseEntityPartitioner<EK extends EntityKey<EK>>
implements EntityPartitioner<EK>{

	private static final int
		MIN_PARTITIONS = 1,
		MAX_ONE_BYTE_NUM_PARTITIONS = 256,
		MAX_PARTITIONS = 1 << 16;
	
	private List<Integer> allPartitions;
	private ArrayList<byte[]> allPrefixes;
	private byte[][] allPrefixesArray;
		
	
	/****************** construct ********************/
	
	public BaseEntityPartitioner(){
		this.allPartitions = new ArrayList<>();
		for(int i=0; i < getNumPartitions(); ++i){
			allPartitions.add(i);
		}
		
		this.allPrefixes = new ArrayList<>();
		if(getNumPartitions()==1){
			allPrefixes.add(new byte[0]);
		}else{
			for(int i=0; i < getNumPartitions(); ++i){
				allPrefixes.add(getPrefix(i));
			}
		}
		
		this.allPrefixesArray = new byte[allPrefixes.size()][];
		for(int i=0; i < allPrefixes.size(); ++i){
			allPrefixesArray[i] = allPrefixes.get(i);
		}
	}
	
	
	/**************** methods *********************/
	
	@Override
	public List<Integer> getAllPartitions(){
		return allPartitions;
	}
	
	@Override
	public boolean isLastPartition(int partition){
		return partition == getNumPartitions() - 1;
	}
	
	@Override
	public int getNumPrefixBytes(){
		return getNumPrefixBytesStatic(getNumPartitions());
	}
	
	@Override
	public List<byte[]> getAllPrefixes(){
		return allPrefixes;
	}
	
	@Override
	public byte[][] getAllPrefixesArray(){
		return allPrefixesArray;
	}
	
	@Override
	public byte[] getPrefix(int partition){
		byte[] fourBytePrefix = IntegerByteTool.getRawBytes(partition);
		int numPrefixBytes = getNumPrefixBytes();
		byte[] prefix = new byte[numPrefixBytes];
		int offset = 4 - numPrefixBytes;
		System.arraycopy(fourBytePrefix, offset, prefix, 0, numPrefixBytes);
		return prefix;
	}
	
	@Override
	public byte[] getNextPrefix(int partition){
		if(isLastPartition(partition)){ return null; }
		return getPrefix(partition + 1);
	}
	
	@Override
	//TODO skip intermediate array
	public byte[] getPrefix(EK ek){
		int partition = getPartition(ek);
		byte[] prefix = getPrefix(partition);
		return prefix;
	}
	
	@Override
	public int parsePartitionFromBytes(byte[] bytes){
		byte[] prefixBytes = DrByteTool.copyOfRange(bytes, 0, getNumPrefixBytes());
		return getPartition(prefixBytes);
		
	}
	
	private int getPartition(byte[] bytes){
		byte[] fourBytePrefix = DrByteTool.padPrefix(bytes, 4);
		return IntegerByteTool.fromRawBytes(fourBytePrefix, 0);
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
	
	
	/************** tests ***********************/
	
	public static class BaseEntityPartitionerTests{
		@Test(expected=IllegalArgumentException.class)
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
		@Test(expected=IllegalArgumentException.class)
		public void testMaxBound(){
			getNumPrefixBytesStatic(65537);
		}
	}
	
}
