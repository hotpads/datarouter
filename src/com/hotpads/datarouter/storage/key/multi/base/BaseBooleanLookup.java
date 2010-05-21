package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseBooleanKey;
import com.hotpads.datarouter.storage.key.multi.Lookup;


@SuppressWarnings("serial")
public abstract class BaseBooleanLookup<D extends Databean>
extends BaseBooleanKey<D>
implements Lookup<D>{

	public BaseBooleanLookup(Class<D> databeanClass, Boolean key) {
		super(databeanClass, key);
	}

}
