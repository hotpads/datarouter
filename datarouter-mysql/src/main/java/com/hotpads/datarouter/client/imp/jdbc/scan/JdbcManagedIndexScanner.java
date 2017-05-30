package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderOps;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.op.scan.BaseManagedIndexScanner;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public class JdbcManagedIndexScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>>
extends BaseManagedIndexScanner<PK,D,IK,IE>{

	private final DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo;
	private final JdbcReaderOps<PK, D, F> jdbcReaderOps;

	public JdbcManagedIndexScanner(JdbcReaderOps<PK, D, F> jdbcReaderOps,
			DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo, Range<IK> range, Config config){
		super(range, config);
		this.jdbcReaderOps = jdbcReaderOps;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
	}

	@Override
	protected List<IE> doLoad(Range<IK> batchRange, Config config){
		return jdbcReaderOps.getIndexRange(batchRange, config, indexEntryFieldInfo);
	}

}
