package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseIntegerKey;
import com.hotpads.datarouter.storage.key.multi.Lookup;


@SuppressWarnings("serial")
public abstract class BaseIntegerLookup<D extends Databean>
extends BaseIntegerKey<D>
implements Lookup<D>{

	public BaseIntegerLookup(Class<D> databeanClass, Integer key) {
		super(databeanClass, key);
	}

}

