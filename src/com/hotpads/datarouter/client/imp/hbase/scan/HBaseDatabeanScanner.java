package com.hotpads.datarouter.client.imp.hbase.scan;

import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class HBaseDatabeanScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseHBaseScanner<PK,D,D>{
	
	public HBaseDatabeanScanner(HBaseReaderNode<PK,D,?> node, 
			DatabeanFieldInfo<PK,D,?> fieldInfo, 
			byte[] startInclusive, byte[] endExclusive, Config pConfig){
		super(node, fieldInfo, startInclusive, endExclusive, pConfig);
	}
	
	@Override
	protected void setCurrentFromResult(Result result) {
		current = HBaseResultTool.getDatabean(result, fieldInfo);
	}
}