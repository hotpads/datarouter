package com.hotpads.datarouter.client.imp.hibernate.scan;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public class HibernatePrimaryKeyScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseHibernateScanner<PK,D,PK>{
	
	public HibernatePrimaryKeyScanner(HibernateReaderNode<PK,D,?> node, DatabeanFieldInfo<PK,D,?> fieldInfo, 
			Range<PK> range, Config pConfig){
		super(node, fieldInfo, range, pConfig);
	}
	
	@Override
	protected boolean isKeysOnly(){
		return true;
	}

	@SuppressWarnings("unchecked") 
	@Override
	protected PK getPrimaryKey(FieldSet<?> fieldSet){
		return (PK)fieldSet;
	}
	
	@SuppressWarnings("unchecked") 
	@Override
	protected void setCurrentFromResult(FieldSet<?> result) {
		current = (PK)result;
	}
}