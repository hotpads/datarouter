package com.hotpads.datarouter.client.imp.hibernate.scan;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.scan.BaseJdbcScanner;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public class HibernateDatabeanScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseJdbcScanner<PK,D,D>{

	private HibernateReaderNode<PK,D,?> node;

	public HibernateDatabeanScanner(HibernateReaderNode<PK,D,?> node, Collection<Range<PK>> ranges, Config config){
		super(ranges, config);
		this.node = node;
	}

	@Override
	protected List<D> doLoad(Collection<Range<PK>> ranges, Config config){
		return node.getRanges(ranges, config);
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