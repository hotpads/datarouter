package com.hotpads.datarouter.storage.view.index.multi.base;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;

@SuppressWarnings("serial")
public abstract class BaseMultiIndexEntry<
		IK extends PrimaryKey<IK>,
		IE extends Databean<IK,IE>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>> 
extends BaseDatabean<IK,IE>
implements MultiIndexEntry<IK,IE,PK,D>{
	
}
