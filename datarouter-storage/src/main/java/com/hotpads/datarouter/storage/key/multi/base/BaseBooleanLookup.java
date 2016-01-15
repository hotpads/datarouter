package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


@SuppressWarnings("serial")
public abstract class BaseBooleanLookup<PK extends PrimaryKey<PK>>
extends BaseSingleFieldLookup<PK, Boolean>{

	public BaseBooleanLookup(Boolean id) {
		super(id);
	}

}
