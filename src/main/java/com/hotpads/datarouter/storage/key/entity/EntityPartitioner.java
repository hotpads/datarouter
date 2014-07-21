package com.hotpads.datarouter.storage.key.entity;

public interface EntityPartitioner<EK extends EntityKey<EK>>{

	int getNumPartitions();
	int getNumPrefixBytes();
	int getPartition(EK ek);
	
}
