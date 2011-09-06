package com.hotpads.datarouter.serialize.fielder;

import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;

public interface DatabeanFielder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends Fielder<D>{

	Class<? extends ScatteringPrefix<PK>> getScatteringPrefixClass();
//	ScatteringPrefix<PK> getScatteringPrefix();
	
	Class<? extends Fielder<PK>> getKeyFielderClass();
	Fielder<PK> getKeyFielder();
	
	List<Field<?>> getKeyFields(D databean);
	List<Field<?>> getNonKeyFields(D databean);
	
}
