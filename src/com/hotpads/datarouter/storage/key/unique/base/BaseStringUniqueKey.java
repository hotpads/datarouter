package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseStringKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseStringUniqueKey<D extends Databean>
extends BaseStringKey<D>
implements UniqueKey<D>{

	public BaseStringUniqueKey(Class<D> databeanClass, String key) {
		super(databeanClass, key);
	}

}
