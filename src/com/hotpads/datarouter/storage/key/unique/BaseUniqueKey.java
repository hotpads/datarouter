package com.hotpads.datarouter.storage.key.unique;

import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
public abstract class BaseUniqueKey<PK extends PrimaryKey<PK>> 
extends BaseKey<PK> 
implements UniqueKey<PK>{
	
}
