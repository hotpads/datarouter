package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderOps;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public class JdbcDatabeanScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseJdbcScanner<PK,D,D>{
	
	private final JdbcReaderOps<PK,D,?> jdbcReaderOps;
	
	public JdbcDatabeanScanner(JdbcReaderOps<PK,D,?> jdbcReaderOps, Range<PK> range, Config pConfig){
		super(range, pConfig);
		this.jdbcReaderOps = jdbcReaderOps;
	}

	@Override
	protected List<D> doLoad(Range<PK> range, Config config){
		return jdbcReaderOps.getRange(range, config);
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