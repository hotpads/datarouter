package com.hotpads.datarouter.storage.key.primary.base;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.ListTool;

@SuppressWarnings("serial") 
public abstract class BaseEntityPrimaryKey<EK extends EntityKey<EK>,PK extends EntityPrimaryKey<EK,PK>>
extends BasePrimaryKey<PK>
implements EntityPrimaryKey<EK,PK>
{

	/*
	 * subclasses may override this to change column names
	 */
	@Override
	public List<Field<?>> getEntityKeyFields(){
		return getEntityKey().getFields();
	}
	
	@Override
	public List<Field<?>> getFields(){
		List<Field<?>> entityKeyFields = getEntityKeyFields();
		List<Field<?>> postEntityKeyFields = getPostEntityKeyFields();
		List<Field<?>> allPkFields = ListTool.concatenate(entityKeyFields, postEntityKeyFields);
		return allPkFields;
	}
	
}
