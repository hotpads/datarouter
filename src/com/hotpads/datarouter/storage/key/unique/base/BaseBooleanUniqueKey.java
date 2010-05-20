package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseBooleanKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseBooleanUniqueKey<D extends Databean>
extends BaseBooleanKey<D>
implements UniqueKey<D>{

	public BaseBooleanUniqueKey(Class<D> databeanClass, Boolean key) {
		super(databeanClass, key);
	}

}
