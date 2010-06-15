package com.hotpads.datarouter.storage.key.multi.base;

import java.util.List;

import com.hotpads.datarouter.storage.field.BaseField;
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

