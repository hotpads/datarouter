package com.hotpads.datarouter.serialize.fielder;

import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface DatabeanFielder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends Fielder<D>{

	Class<? extends Fielder<PK>> getKeyFielderClass();
	Fielder<PK> getKeyFielder();
	List<Field<?>> getKeyFields(D databean);
	List<Field<?>> getNonKeyFields(D databean);
	
}
