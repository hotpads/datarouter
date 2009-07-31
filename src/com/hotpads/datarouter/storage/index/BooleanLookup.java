package com.hotpads.datarouter.storage.index;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseBooleanKey;

@SuppressWarnings("serial")
public abstract class BooleanLookup<D extends Databean>
extends BaseBooleanKey<D>
implements Lookup<D>{

	public BooleanLookup(Class<D> databeanClass, Boolean key) {
		super(databeanClass, key);
	}

}
