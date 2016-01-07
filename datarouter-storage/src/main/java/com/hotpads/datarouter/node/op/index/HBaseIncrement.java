package com.hotpads.datarouter.node.op.index;

import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface HBaseIncrement<PK extends PrimaryKey>{
	public void increment(Map<PK,Map<String,Long>> countByColumnByKey, Config pConfig);
}
