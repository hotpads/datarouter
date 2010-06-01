package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.key.base.BaseIntegerKey;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


@SuppressWarnings("serial")
public abstract class BaseIntegerLookup<PK extends PrimaryKey<PK>>
extends BaseIntegerKey<PK>
implements Lookup<PK>{

	public BaseIntegerLookup(Integer key) {
		super(key);
	}

}

