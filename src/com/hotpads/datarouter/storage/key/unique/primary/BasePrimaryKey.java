package com.hotpads.datarouter.storage.key.unique.primary;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.BaseUniqueKey;

@SuppressWarnings("serial")
public abstract class BasePrimaryKey<D extends Databean> 
extends BaseUniqueKey<D> 
implements PrimaryKey<D>{

	public BasePrimaryKey(Class<D> databeanClass){
		super(databeanClass);
	}

}
