package com.hotpads.datarouter.storage.key.primary.base;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

@SuppressWarnings("serial") 
public abstract class BaseEntityPrimaryKey<EK extends EntityKey<EK>,PK extends PrimaryKey<PK>>
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
		int numFields = CollectionTool.size(entityKeyFields) + CollectionTool.size(postEntityKeyFields);
		List<Field<?>> allPkFields = ListTool.createArrayList(numFields);
		allPkFields.addAll(CollectionTool.nullSafe(entityKeyFields));
		allPkFields.addAll(CollectionTool.nullSafe(postEntityKeyFields));
		return allPkFields;
	}
	
}
