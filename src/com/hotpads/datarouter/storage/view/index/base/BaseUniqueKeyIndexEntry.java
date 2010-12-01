package com.hotpads.datarouter.storage.view.index.base;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.key.UniqueKeyIndexEntry;

@SuppressWarnings("serial")
public abstract class BaseUniqueKeyIndexEntry<
		IK extends PrimaryKey<IK>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>> 
extends BaseDatabean<IK>
implements UniqueKeyIndexEntry<IK,PK,D>{
    
	@Override
	public void fromDatabean(D target){
		fromPrimaryKey(target.getKey());
	}
	
}
