package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.key.base.BaseBooleanKey;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


@SuppressWarnings("serial")
public abstract class BaseBooleanLookup<PK extends PrimaryKey<PK>>
extends BaseBooleanKey<PK>
implements Lookup<PK>{

	public BaseBooleanLookup(Boolean key) {
		super(key);
	}

}
