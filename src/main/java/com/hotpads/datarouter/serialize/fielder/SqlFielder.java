package com.hotpads.datarouter.serialize.fielder;

import com.hotpads.datarouter.serialize.fieldcache.FieldCache;
import com.hotpads.datarouter.serialize.fieldcache.GenericFieldCache;
import com.hotpads.datarouter.storage.field.FieldSet;

public class SqlFielder<F extends FieldSet,C extends Class<F>>{

	protected FieldCache<F,C> cache;
	
	SqlFielder(F fieldSet){
		this.cache = new GenericFieldCache(fieldSet.getClass());
	}
	
}
