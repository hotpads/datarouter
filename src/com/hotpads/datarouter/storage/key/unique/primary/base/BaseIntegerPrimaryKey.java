package com.hotpads.datarouter.storage.key.unique.primary.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.base.BaseIntegerUniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseIntegerPrimaryKey<D extends Databean>
extends BaseIntegerUniqueKey<D>
implements PrimaryKey<D>{

	public BaseIntegerPrimaryKey(Class<D> databeanClass, Integer key) {
		super(databeanClass, key);
	}

}

