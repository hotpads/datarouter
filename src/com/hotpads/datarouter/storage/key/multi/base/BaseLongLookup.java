package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseLongKey;
import com.hotpads.datarouter.storage.key.multi.Lookup;


@SuppressWarnings("serial")
public abstract class BaseLongLookup<D extends Databean>
extends BaseLongKey<D>
implements Lookup<D>{

	public BaseLongLookup(Class<D> databeanClass, Long key) {
		super(databeanClass, key);
	}

}

