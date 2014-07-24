package com.hotpads.datarouter.storage.key.entity;

import java.util.List;

public interface EntityPartitioner<EK extends EntityKey<EK>>{

	int getNumPartitions();
	int getNumPrefixBytes();
	List<byte[]> getAllPrefixes();
	byte[] getPrefix(int partition);
	int getPartition(EK ek);
	byte[] getPrefix(EK ek);
	
}
