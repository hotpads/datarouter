package com.hotpads.datarouter.storage.databean.update;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.storage.content.ContentHolder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrStringTool;

public class CaseEnforcingDatabeanUpdate <PK extends PrimaryKey<PK>, D extends Databean<PK,D> & ContentHolder<PK,D>>
extends DatabeanUpdate<PK,D>{

	public <Storage extends MapStorage<PK,D>> CaseEnforcingDatabeanUpdate(Storage storage) {
		super(storage);
	}

	/**
	 * replace if the case of any StringField in the key is different but the case insensitive content is the same
	 */
	@Override
	protected boolean replaceInsteadOfMerge(D oldBean, D newBean) {
		return keysEqualWithDifferentCase(oldBean, newBean);
	}

	public static <PK extends PrimaryKey<PK>, D extends Databean<PK,D> & ContentHolder<PK,D>> boolean
	keysEqualWithDifferentCase(D oldBean, D newBean){
		Map<String,String> oldStringFieldValues = Maps.newHashMap();
		for(Field<?> oldKeyField : oldBean.getKeyFields()){
			if( ! oldKeyField.getClass().isAssignableFrom(StringField.class)){
				continue;
			}
			oldStringFieldValues.put(oldKeyField.getKey().getName(), oldKeyField.getValueString());
		}
		for(Field<?> newKeyField : newBean.getKeyFields()){
			String oldValue = oldStringFieldValues.get(newKeyField.getKey().getName());
			if(DrStringTool.equalsCaseInsensitiveButNotCaseSensitive(oldValue, newKeyField.getValueString())){
				return true;
			}
		}
		return false;
	}

}