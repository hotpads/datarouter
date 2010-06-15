package com.hotpads.datarouter.storage.key;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.BaseFieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
public abstract class BaseKey<PK extends PrimaryKey<PK>> 
extends BaseFieldSet
implements Key<PK>{

	@Override
	public List<Field<?>> getKeyFields(){
		return this.getFields();
	}
	
}
