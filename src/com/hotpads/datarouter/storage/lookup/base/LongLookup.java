package com.hotpads.datarouter.storage.lookup.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseLongKey;
import com.hotpads.datarouter.storage.lookup.Lookup;


@SuppressWarnings("serial")
public abstract class LongLookup<D extends Databean>
extends BaseLongKey<D>
implements Lookup<D>{

	public LongLookup(Class<D> databeanClass, Long key) {
		super(databeanClass, key);
	}

}

