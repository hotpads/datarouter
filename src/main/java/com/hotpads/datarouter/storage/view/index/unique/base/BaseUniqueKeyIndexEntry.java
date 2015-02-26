package com.hotpads.datarouter.storage.view.index.unique.base;

import java.util.List;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueKeyIndexEntry;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.java.ReflectionTool;

@SuppressWarnings("serial")
public abstract class BaseUniqueKeyIndexEntry<
		IK extends PrimaryKey<IK>,
		IE extends Databean<IK,IE>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>> 
extends BaseDatabean<IK,IE>
implements UniqueKeyIndexEntry<IK,IE,PK,D>{
    
//	@Override
//	public void fromDatabean(D target){
//		fromPrimaryKey(target.getKey());
//	}

	@SuppressWarnings("unchecked")
	public List<IE> createFromDatabean(D target){
		BaseUniqueKeyIndexEntry<IK,IE,PK,D> indexEntry = (BaseUniqueKeyIndexEntry<IK,IE,PK,D>)ReflectionTool.create(getClass());
		indexEntry.fromPrimaryKey(target.getKey());
		return (List<IE>)DrListTool.wrap(indexEntry);
	}
	
//	@Override
//	public List<Field<?>> getFields(PK pk) {
//		return pk.getFields();
//	};
	
}
