package com.hotpads.datarouter.serialize.fieldcache;

import com.hotpads.datarouter.storage.field.FieldSet;

public interface FieldCache<F extends FieldSet<?>,C extends Class<F>>{
	
	Object get(F fieldSet, String fieldName);
	
}
