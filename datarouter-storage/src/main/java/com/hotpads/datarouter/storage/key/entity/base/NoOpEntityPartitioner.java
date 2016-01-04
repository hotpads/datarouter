package com.hotpads.datarouter.storage.key.entity.base;

import com.hotpads.datarouter.storage.key.entity.EntityKey;

public class NoOpEntityPartitioner<EK extends EntityKey<EK>>
extends BaseEntityPartitioner<EK>{

	@Override
	public int getNumPartitions(){
		return 1;
	}
	
	@Override
	public int getPartition(EK ek){
		return 0;
	}
	
}
