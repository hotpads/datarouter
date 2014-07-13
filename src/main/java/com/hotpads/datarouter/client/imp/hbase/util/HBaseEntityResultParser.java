package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.Map;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public class HBaseEntityResultParser<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{


	private Map<String,Node<?,?>> nodeByQualifierPrefix;

	public HBaseEntityResultParser(Map<String,Node<?,?>> nodeByQualifierPrefix){
		this.nodeByQualifierPrefix = nodeByQualifierPrefix;
	}
	
	
	
}
