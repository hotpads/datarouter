package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.key.base.BaseLongKey;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


@SuppressWarnings("serial")
public abstract class BaseLongLookup<PK extends PrimaryKey<PK>>
extends BaseLongKey<PK>
implements Lookup<PK>{

	public BaseLongLookup(Long id) {
		super(id);
	}

}

