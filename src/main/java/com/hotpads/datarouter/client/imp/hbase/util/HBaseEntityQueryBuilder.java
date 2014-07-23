package com.hotpads.datarouter.client.imp.hbase.util;

import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.util.core.ByteTool;

public class HBaseEntityQueryBuilder<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	private EntityFieldInfo<EK,E> entityFieldInfo;
	
	
	
	public HBaseEntityQueryBuilder(EntityFieldInfo<EK,E> entityFieldInfo){
		this.entityFieldInfo = entityFieldInfo;
	}


	/************** methods *******************/
	
	public byte[] getRowBytes(EK ek){
		if(ek==null){ throw new IllegalArgumentException("no nulls"); }
		return FieldTool.getConcatenatedValueBytes(ek.getFields(), true, false);
	}
	
	public byte[] getRowBytesWithPartition(EK ek){
		byte[] partitionPrefix = entityFieldInfo.getEntityPartitioner().getPrefix(ek);
		return ByteTool.concatenate(partitionPrefix, getRowBytes(ek));
	}
	
}
