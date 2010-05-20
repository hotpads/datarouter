package com.hotpads.datarouter.storage.key.unique.primary.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.base.BaseStringUniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseStringPrimaryKey<D extends Databean>
extends BaseStringUniqueKey<D>
implements PrimaryKey<D>{

	public BaseStringPrimaryKey(Class<D> databeanClass, String key) {
		super(databeanClass, key);
	}

}
