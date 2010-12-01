package com.hotpads.datarouter.storage.key;

import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public interface Key<PK extends PrimaryKey<PK>>
extends FieldSet{
	
//	void fromPersistentString(String s);
	
}
