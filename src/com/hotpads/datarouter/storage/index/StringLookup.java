package com.hotpads.datarouter.storage.index;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.BaseStringKey;

@SuppressWarnings("serial")
public abstract class StringLookup<D extends Databean>
extends BaseStringKey<D>
implements Lookup<D>{

	public StringLookup(Class<D> databeanClass, String key) {
		super(databeanClass, key);
	}

}
