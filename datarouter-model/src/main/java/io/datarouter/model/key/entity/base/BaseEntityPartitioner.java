/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.model.key.entity.base;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.IntegerByteTool;

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
		for(int i = 0; i < getNumPartitions(); ++i){
			allPartitions.add(i);
		}

		this.allPrefixes = new ArrayList<>();
		if(getNumPartitions() == 1){
			allPrefixes.add(new byte[0]);
		}else{
			for(int i = 0; i < getNumPartitions(); ++i){
				allPrefixes.add(getPrefix(i));
			}
		}

		this.allPrefixesArray = new byte[allPrefixes.size()][];
		for(int i = 0; i < allPrefixes.size(); ++i){
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
	//TODO skip intermediate array
	public byte[] getPrefix(EK ek){
		int partition = getPartition(ek);
		byte[] prefix = getPrefix(partition);
		return prefix;
	}

	@Override
	public byte[] getNextPrefix(int partition){
		if(isLastPartition(partition)){
			return null;
		}
		return getPrefix(partition + 1);
	}

	@Override
	public int parsePartitionFromBytes(byte[] bytes){
		byte[] prefixBytes = ByteTool.copyOfRange(bytes, 0, getNumPrefixBytes());
		return getPartition(prefixBytes);

	}

	private int getPartition(byte[] bytes){
		byte[] fourBytePrefix = ByteTool.padPrefix(bytes, 4);
		return IntegerByteTool.fromRawBytes(fourBytePrefix, 0);
	}

	/*********** static for testing *************/

	private static int getNumPrefixBytesStatic(int numPartitions){
		if(numPartitions < MIN_PARTITIONS){
			throw new IllegalArgumentException("must have at least one partition");
		}
		if(numPartitions == 1){
			return 0;
		}
		if(numPartitions <= MAX_ONE_BYTE_NUM_PARTITIONS){
			return 1;
		}
		if(numPartitions <= MAX_PARTITIONS){
			return 2;
		}
		throw new IllegalArgumentException("max partitions is " + MAX_PARTITIONS);
	}


	/************** tests ***********************/

	public static class BaseEntityPartitionerTests{
		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testMinBound(){
			getNumPrefixBytesStatic(0);
		}
		@Test
		public void testValidNumPartitions(){
			Assert.assertEquals(getNumPrefixBytesStatic(1), 0);
			Assert.assertEquals(getNumPrefixBytesStatic(2), 1);
			Assert.assertEquals(getNumPrefixBytesStatic(255), 1);
			Assert.assertEquals(getNumPrefixBytesStatic(256), 1);
			Assert.assertEquals(getNumPrefixBytesStatic(257), 2);
			Assert.assertEquals(getNumPrefixBytesStatic(65535), 2);
			Assert.assertEquals(getNumPrefixBytesStatic(65536), 2);
		}
		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testMaxBound(){
			getNumPrefixBytesStatic(65537);
		}
	}

}
