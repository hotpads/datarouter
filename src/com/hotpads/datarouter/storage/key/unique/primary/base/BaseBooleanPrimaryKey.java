package com.hotpads.datarouter.storage.key.unique.primary.base;

import javax.persistence.MappedSuperclass;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.base.BaseBooleanUniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;


@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseBooleanPrimaryKey<D extends Databean>
extends BaseBooleanUniqueKey<D>
implements PrimaryKey<D>{

	public BaseBooleanPrimaryKey(Class<D> databeanClass, Boolean key) {
		super(databeanClass, key);
	}

}
