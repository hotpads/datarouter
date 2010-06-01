package com.hotpads.datarouter.storage.key;

import com.hotpads.datarouter.storage.field.BaseFieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
public abstract class BaseKey<PK extends PrimaryKey<PK>> 
extends BaseFieldSet
implements Key<PK>{  //hibernate composite keys must implement serializable
	
	public static final String DEFAULT_KEY_NAME = "key";
	
}
