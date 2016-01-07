package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


@SuppressWarnings("serial")
public abstract class BaseLongLookup<PK extends PrimaryKey<PK>>
extends BaseSingleFieldLookup<PK,Long>{

	public BaseLongLookup(Long id) {
		super(id);
	}
}

