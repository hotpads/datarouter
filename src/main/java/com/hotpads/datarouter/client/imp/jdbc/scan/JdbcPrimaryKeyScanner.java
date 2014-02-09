package com.hotpads.datarouter.client.imp.jdbc.scan;

import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public class JdbcPrimaryKeyScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseJdbcScanner<PK,D,PK>{
	
	public JdbcPrimaryKeyScanner(JdbcReaderNode<PK,D,?> node, DatabeanFieldInfo<PK,D,?> fieldInfo, 
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