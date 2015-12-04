package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderOps;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public class JdbcPrimaryKeyScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseJdbcScanner<PK,D,PK>{

	private final JdbcReaderOps<PK,D,?> jdbcReaderOps;

	public JdbcPrimaryKeyScanner(JdbcReaderOps<PK,D,?> jdbcReaderOps, DatabeanFieldInfo<PK,D,?> fieldInfo,
			Collection<Range<PK>> ranges, Config pConfig){
		super(ranges, pConfig);
		this.jdbcReaderOps = jdbcReaderOps;
	}

	@Override
	protected List<PK> doLoad(Collection<Range<PK>> ranges, Config config){
		return jdbcReaderOps.getKeysInRanges(ranges, config);
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