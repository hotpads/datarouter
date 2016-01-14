package com.hotpads.datarouter.client.imp.hibernate.scan;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.scan.BaseJdbcScanner;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public class HibernatePrimaryKeyScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseJdbcScanner<PK,D,PK>{

	private HibernateReaderNode<PK,D,?> node;

	public HibernatePrimaryKeyScanner(HibernateReaderNode<PK,D,?> node, Collection<Range<PK>> ranges, Config config){
		super(ranges, config);
		this.node = node;
	}

	@Override
	protected List<PK> doLoad(Collection<Range<PK>> ranges, Config config){
		return node.getKeysInRanges(ranges, config);
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