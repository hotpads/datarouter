package com.hotpads.datarouter.client.imp.hibernate.scan;

import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public class HibernatePrimaryKeyScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseHibernateScanner<PK,D,PK>{
	
	public HibernatePrimaryKeyScanner(HibernateReaderNode<PK,D,?> node, DatabeanFieldInfo<PK,D,?> fieldInfo, 
			Range<PK> range, Config pConfig){
		super(node, fieldInfo, range, pConfig);
	}

	@Override
	protected List<PK> doLoad(Range<PK> range, Config config){
		return node.getKeysInRange(range.getStart(), range.getStartInclusive(), range.getEnd(), 
				range.getEndInclusive(), config);
	}

	@Override
	protected PK getPrimaryKey(PK pk){
		return pk;
	}
	
	@Override
	protected void setCurrentFromResult(PK result) {
		current = result;
	}
}