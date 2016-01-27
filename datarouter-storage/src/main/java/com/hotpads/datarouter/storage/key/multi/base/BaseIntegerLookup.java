package com.hotpads.datarouter.storage.key.multi.base;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


@SuppressWarnings("serial")
public abstract class BaseIntegerLookup<PK extends PrimaryKey<PK>>
extends BaseSingleFieldLookup<PK,Integer>{

	public BaseIntegerLookup(Integer id) {
		super(id);
	}

}

