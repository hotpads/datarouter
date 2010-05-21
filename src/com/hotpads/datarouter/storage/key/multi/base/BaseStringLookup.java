package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseStringKey;
import com.hotpads.datarouter.storage.key.multi.Lookup;


@SuppressWarnings("serial")
public abstract class BaseStringLookup<D extends Databean>
extends BaseStringKey<D>
implements Lookup<D>{

	public BaseStringLookup(Class<D> databeanClass, String key) {
		super(databeanClass, key);
	}

}
