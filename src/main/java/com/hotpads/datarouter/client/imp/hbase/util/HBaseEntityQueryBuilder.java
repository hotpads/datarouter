package com.hotpads.datarouter.client.imp.hbase.util;

import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public class HBaseEntityQueryBuilder<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	
	public byte[] getRowBytes(EK entityKey){
		if(entityKey==null){ return new byte[]{}; }
		return FieldTool.getConcatenatedValueBytes(entityKey.getFields(), true, false);
	}
	
}
