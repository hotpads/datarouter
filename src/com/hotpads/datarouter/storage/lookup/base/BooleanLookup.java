package com.hotpads.datarouter.storage.lookup.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseBooleanKey;
import com.hotpads.datarouter.storage.lookup.Lookup;
import com.hotpads.datarouter.storage.lookup.Lookup;


@SuppressWarnings("serial")
public abstract class BooleanLookup<D extends Databean>
extends BaseBooleanKey<D>
implements Lookup<D>{

	public BooleanLookup(Class<D> databeanClass, Boolean key) {
		super(databeanClass, key);
	}

}
