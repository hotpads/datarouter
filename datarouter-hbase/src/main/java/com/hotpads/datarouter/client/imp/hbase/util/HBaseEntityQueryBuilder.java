package com.hotpads.datarouter.client.imp.hbase.util;

import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.util.core.DrByteTool;

public class HBaseEntityQueryBuilder<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	protected final EntityPartitioner<EK> partitioner;

	public HBaseEntityQueryBuilder(EntityFieldInfo<EK,E> entityFieldInfo){
		this.partitioner = entityFieldInfo.getEntityPartitioner();
	}

	/************** methods *******************/

	public byte[] getRowBytes(EK ek){
		if(ek == null){
			throw new IllegalArgumentException("no nulls");
		}
		return FieldTool.getConcatenatedValueBytes(ek.getFields(), true, true, false);
	}

	public byte[] getRowBytesWithPartition(EK ek){
		byte[] partitionPrefix = partitioner.getPrefix(ek);
		return DrByteTool.concatenate(partitionPrefix, getRowBytes(ek));
	}

}
