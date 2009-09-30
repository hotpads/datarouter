package com.hotpads.datarouter.storage.lookup.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseIntegerKey;
import com.hotpads.datarouter.storage.lookup.Lookup;
import com.hotpads.datarouter.storage.lookup.Lookup;


@SuppressWarnings("serial")
public abstract class IntegerLookup<D extends Databean>
extends BaseIntegerKey<D>
implements Lookup<D>{

	public IntegerLookup(Class<D> databeanClass, Integer key) {
		super(databeanClass, key);
	}

}

