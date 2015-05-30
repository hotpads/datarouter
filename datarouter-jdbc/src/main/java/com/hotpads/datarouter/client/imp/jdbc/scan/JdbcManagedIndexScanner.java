package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
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
	private final Config config;

	public JdbcManagedIndexScanner(JdbcReaderOps<PK, D, F> jdbcReaderOps,
			DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo, Range<IK> range, Config config){
		super(range);
		this.jdbcReaderOps = jdbcReaderOps;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
		this.config = Config.nullSafe(config).setIterateBatchSizeIfNull(JdbcNode.DEFAULT_ITERATE_BATCH_SIZE);
	}

	@Override
	protected List<IE> doLoad(Range<IK> batchRange){
		return jdbcReaderOps.getIndexRange(batchRange, config, indexEntryFieldInfo);
	}

}
