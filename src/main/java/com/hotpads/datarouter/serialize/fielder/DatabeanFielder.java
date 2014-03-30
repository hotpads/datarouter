package com.hotpads.datarouter.serialize.fielder;

import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;

public interface DatabeanFielder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends Fielder<D>{

	Class<? extends ScatteringPrefix> getScatteringPrefixClass();
	
	//TODO require PrimaryKeyFielder (requires find/replace in all Databeans
	Class<? extends Fielder<PK>> getKeyFielderClass();
	Fielder<PK> getKeyFielder();
	
	List<Field<?>> getKeyFields(D databean);
	List<Field<?>> getNonKeyFields(D databean);
	
	Map<String,List<Field<?>>> getIndexes(D databean);
	MySqlCollation getCollation(D databean);
	MySqlCharacterSet getCharacterSet(D databean);
	
}
