package com.hotpads.datarouter.client.imp.hibernate.scan;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.scan.BaseJdbcScanner;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.exception.NotImplementedException;

public class HibernatePrimaryKeyScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseJdbcScanner<PK,D,PK>{

	private HibernateReaderNode<PK,D,?> node;

	public HibernatePrimaryKeyScanner(HibernateReaderNode<PK,D,?> node, Range<PK> range, Config config){
		super(Arrays.asList(range), config);
		this.node = node;
	}

	@Override
	protected List<PK> doLoad(Collection<Range<PK>> ranges, Config config){
		if(ranges.size() != 1){
			throw new NotImplementedException();
		}
		Range<PK> range = DrCollectionTool.getFirst(ranges);
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