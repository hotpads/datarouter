package com.hotpads.datarouter.storage.key;

import com.hotpads.datarouter.storage.field.FieldSet;


public interface Key<K extends Key<K>>//Key should probably not reference PrimaryKey?
extends FieldSet<K>{
	
//	void fromPersistentString(String s);
	
}
