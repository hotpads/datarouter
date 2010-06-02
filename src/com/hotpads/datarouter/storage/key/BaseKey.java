package com.hotpads.datarouter.storage.key;

import com.hotpads.datarouter.storage.field.BaseFieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
public abstract class BaseKey<PK extends PrimaryKey<PK>> 
extends BaseFieldSet
implements Key<PK>{
		
}
