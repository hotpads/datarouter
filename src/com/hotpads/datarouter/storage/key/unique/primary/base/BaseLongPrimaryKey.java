package com.hotpads.datarouter.storage.key.unique.primary.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.base.BaseLongUniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseLongPrimaryKey<D extends Databean>
extends BaseLongUniqueKey<D>
implements PrimaryKey<D>{
	
	public BaseLongPrimaryKey(Class<D> databeanClass, Long key) {
		super(databeanClass, key);
	}

}

