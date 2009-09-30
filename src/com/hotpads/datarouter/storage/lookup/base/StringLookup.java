package com.hotpads.datarouter.storage.lookup.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseStringKey;
import com.hotpads.datarouter.storage.lookup.Lookup;
import com.hotpads.datarouter.storage.lookup.Lookup;


@SuppressWarnings("serial")
public abstract class StringLookup<D extends Databean>
extends BaseStringKey<D>
implements Lookup<D>{

	public StringLookup(Class<D> databeanClass, String key) {
		super(databeanClass, key);
	}

}
