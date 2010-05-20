package com.hotpads.datarouter.storage.key.unique.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.base.BaseLongKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseLongUniqueKey<D extends Databean>
extends BaseLongKey<D>
implements UniqueKey<D>{

	public BaseLongUniqueKey(Class<D> databeanClass, Long key) {
		super(databeanClass, key);
	}

}

