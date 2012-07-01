package com.hotpads.datarouter.client.imp.hibernate.scan;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class HibernateDatabeanScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseHibernateScanner<PK,D,FieldSet<?>>{
	
	public HibernateDatabeanScanner(HibernateReaderNode<PK,D,?> node, DatabeanFieldInfo<PK,D,?> fieldInfo,
			PK startInclusive, PK endExclusive, Config pConfig){
		super(node, fieldInfo, startInclusive, endExclusive, pConfig);
	}
	
	@Override
	protected boolean isKeysOnly(){
		return false;
	}

	@SuppressWarnings("unchecked") 
	@Override
	protected PK getPrimaryKey(FieldSet<?> fieldSet){
		return ((D)fieldSet).getKey();
	}
	
	@SuppressWarnings("unchecked") 
	@Override
	protected void setCurrentFromResult(FieldSet<?> result) {
		current = (D)result;
	}
}