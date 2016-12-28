package com.hotpads.datarouter.storage.key.primary.base;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public abstract class BaseEntityPrimaryKey<EK extends EntityKey<EK>,PK extends EntityPrimaryKey<EK,PK>>
extends BasePrimaryKey<PK>
implements EntityPrimaryKey<EK,PK>{

	private static final String DEFAULT_ENTITY_KEY_FIELD_NAME = "entityKey";

	public String getEntityKeyName(){
		return DEFAULT_ENTITY_KEY_FIELD_NAME;
	}

	/*
	 * subclasses may override this to change column names
	 */
	@Override
	public List<Field<?>> getEntityKeyFields(){
		if(DrStringTool.isEmpty(getEntityKeyName())){//Should this logic be in FieldTool.prependPrefixes
			return getEntityKey().getFields();
		}
		return FieldTool.prependPrefixes(getEntityKeyName(), getEntityKey().getFields());
	}

	@Override
	public List<Field<?>> getFields(){
		List<Field<?>> entityKeyFields = getEntityKeyFields();
		List<Field<?>> postEntityKeyFields = getPostEntityKeyFields();
		List<Field<?>> allPkFields = DrListTool.concatenate(entityKeyFields, postEntityKeyFields);
		return allPkFields;
	}

}
