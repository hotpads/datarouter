package com.hotpads.datarouter.storage.key.multi;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseKey;


@SuppressWarnings("serial")
public abstract class BaseLookup<D extends Databean>
extends BaseKey<D>
implements Lookup<D>{

	public BaseLookup(Class<D> databeanClass) {
		super(databeanClass);
	}

}
