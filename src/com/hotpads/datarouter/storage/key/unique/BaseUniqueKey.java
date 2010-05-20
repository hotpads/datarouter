package com.hotpads.datarouter.storage.key.unique;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;

@SuppressWarnings("serial")
public abstract class BaseUniqueKey<D extends Databean> 
extends BaseKey<D> 
implements UniqueKey<D>{

	public BaseUniqueKey(Class<D> databeanClass){
		super(databeanClass);
	}

}
