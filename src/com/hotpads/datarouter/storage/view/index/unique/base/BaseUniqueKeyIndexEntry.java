package com.hotpads.datarouter.storage.view.index.unique.base;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueKeyIndexEntry;

@SuppressWarnings("serial")
public abstract class BaseUniqueKeyIndexEntry<
		IK extends PrimaryKey<IK>,
		IE extends Databean<IK,IE>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>> 
extends BaseDatabean<IK,IE>
implements UniqueKeyIndexEntry<IK,IE,PK,D>{
    
	@Override
	public void fromDatabean(D target){
		fromPrimaryKey(target.getKey());
	}
	
//	@Override
//	public List<Field<?>> getFields(PK pk) {
//		return pk.getFields();
//	};
	
}
