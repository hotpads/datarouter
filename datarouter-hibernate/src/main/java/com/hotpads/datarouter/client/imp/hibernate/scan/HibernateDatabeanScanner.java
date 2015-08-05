package com.hotpads.datarouter.client.imp.hibernate.scan;

import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public class HibernateDatabeanScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseHibernateScanner<PK,D,D>{
	
	public HibernateDatabeanScanner(HibernateReaderNode<PK,D,?> node, Range<PK> range, Config config){
		super(node, range, config);
	}

	@Override
	protected List<D> doLoad(Range<PK> range, Config config){
		return node.getRange(range.getStart(), range.getStartInclusive(), range.getEnd(), 
				range.getEndInclusive(), config);
	}

	@Override
	protected PK getPrimaryKey(D databean){
		return databean.getKey();
	}
	 
	@Override
	protected void setCurrentFromResult(D result) {
		current = result;
	}
}