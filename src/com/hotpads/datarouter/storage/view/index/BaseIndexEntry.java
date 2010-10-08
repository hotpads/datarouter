package com.hotpads.datarouter.storage.view.index;

import java.util.List;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
public abstract class BaseIndexEntry<IK extends PrimaryKey<IK>,TK extends PrimaryKey<TK>> 
extends BaseDatabean<IK>
implements IndexEntry<IK,TK>{

    @Override
    public List<Field<?>> getNonKeyFields(){
		return getTargetKey().getFields();
    }
    
}
