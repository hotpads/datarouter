package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseIntegerKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseIntegerUniqueKey<D extends Databean>
extends BaseIntegerKey<D>
implements UniqueKey<D>{

	public BaseIntegerUniqueKey(Class<D> databeanClass, Integer key) {
		super(databeanClass, key);
	}

}

