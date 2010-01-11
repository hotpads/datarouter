package com.hotpads.datarouter.storage.lookup.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.datarouter.storage.lookup.Lookup;


@SuppressWarnings("serial")
public abstract class BaseLookup<D extends Databean>
extends BaseKey<D>
implements Lookup<D>{

	public BaseLookup(Class<D> databeanClass) {
		super(databeanClass);
	}

}
