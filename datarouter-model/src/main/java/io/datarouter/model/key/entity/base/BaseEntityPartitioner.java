/*
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
import java.util.Arrays;
import java.util.List;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;

public abstract class BaseEntityPartitioner<EK extends EntityKey<EK>>
implements EntityPartitioner<EK>{

	private static final RawIntCodec RAW_INT_CODEC = RawIntCodec.INSTANCE;
	private static final int MIN_PARTITIONS = 1;
	private static final int MAX_ONE_BYTE_NUM_PARTITIONS = 256;
	private static final int MAX_PARTITIONS = 1 << 16;

	private final int numPartitions;
	private final List<Integer> allPartitions;
	private final ArrayList<byte[]> allPrefixes;
	private final byte[][] allPrefixesArray;


	/*------------- construct -------------*/

	public BaseEntityPartitioner(int numPartitions){
		this.numPartitions = numPartitions;
		this.allPartitions = new ArrayList<>();
		for(int i = 0; i < numPartitions; ++i){
			allPartitions.add(i);
		}

		this.allPrefixes = new ArrayList<>();
		if(numPartitions == 1){
			allPrefixes.add(EmptyArray.BYTE);
		}else{
			for(int i = 0; i < numPartitions; ++i){
				allPrefixes.add(getPrefix(i));
			}
		}

		this.allPrefixesArray = new byte[allPrefixes.size()][];
		for(int i = 0; i < allPrefixes.size(); ++i){
			allPrefixesArray[i] = allPrefixes.get(i);
		}
	}

	/*------------- methods -------------*/

	@Override
	public final int getNumPartitions(){
		return numPartitions;
	}

	@Override
	public List<Integer> getAllPartitions(){
		return allPartitions;
	}

	@Override
	public boolean isLastPartition(int partition){
		return partition == numPartitions - 1;
	}

	@Override
	public int getNumPrefixBytes(){
		return getNumPrefixBytesStatic(numPartitions);
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
		byte[] fourBytePrefix = RAW_INT_CODEC.encode(partition);
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
		return getPrefix(partition);
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
		byte[] prefixBytes = Arrays.copyOfRange(bytes, 0, getNumPrefixBytes());
		return getPartition(prefixBytes);

	}

	private int getPartition(byte[] bytes){
		byte[] fourBytePrefix = ByteTool.padPrefix(bytes, 4);
		return RAW_INT_CODEC.decode(fourBytePrefix, 0);
	}

	/*------------- for testing -------------*/

	static int getNumPrefixBytesStatic(int numPartitions){
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

}
