package com.hotpads.datarouter.storage.key.entity;

import java.util.List;

public interface EntityPartitioner<EK extends EntityKey<EK>>{

	int getNumPartitions();
	List<Integer> getAllPartitions();
	boolean isLastPartition(int partition);
	int getNumPrefixBytes();
	List<byte[]> getAllPrefixes();
	byte[][] getAllPrefixesArray();
	byte[] getPrefix(int partition);
	byte[] getNextPrefix(int partition);
	int getPartition(EK ek);
	byte[] getPrefix(EK ek);
	int parsePartitionFromBytes(byte[] bytes);
	
}
