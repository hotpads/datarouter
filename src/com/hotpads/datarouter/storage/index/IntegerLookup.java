package com.hotpads.datarouter.storage.index;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseIntegerKey;

@SuppressWarnings("serial")
public abstract class IntegerLookup<D extends Databean>
extends BaseIntegerKey<D>
implements Lookup<D>{

	public IntegerLookup(Class<D> databeanClass, Integer key) {
		super(databeanClass, key);
	}

}
