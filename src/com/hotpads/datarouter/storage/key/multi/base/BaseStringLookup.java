package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.key.base.BaseStringKey;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


@SuppressWarnings("serial")
public abstract class BaseStringLookup<PK extends PrimaryKey<PK>>
extends BaseStringKey<PK>
implements Lookup<PK>{

	public BaseStringLookup(String id) {
		super(id);
	}

}
