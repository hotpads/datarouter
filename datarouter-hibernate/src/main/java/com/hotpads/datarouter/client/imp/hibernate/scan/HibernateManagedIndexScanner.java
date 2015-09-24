package com.hotpads.datarouter.client.imp.hibernate.scan;

import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcManagedIndexGetRangeOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.op.scan.BaseManagedIndexScanner;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public class HibernateManagedIndexScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>>
extends BaseManagedIndexScanner<PK,D,IK,IE>{

	private final DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final HibernateReaderNode<PK,D,F> node;

	public HibernateManagedIndexScanner(Range<IK> range, Config config, JdbcFieldCodecFactory fieldCodecFactory,
			DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, HibernateReaderNode<PK,D,F> node){
		super(range, config);
		this.fieldCodecFactory = fieldCodecFactory;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
		this.node = node;
	}

	@Override
	protected List<IE> doLoad(Range<IK> batchRange, Config config){
		JdbcManagedIndexGetRangeOp<PK,D,IK,IE,IF> op = new JdbcManagedIndexGetRangeOp<>(node, fieldCodecFactory,
				indexEntryFieldInfo, batchRange, config);
		return new SessionExecutorImpl<>(op, IndexedStorageReader.OP_getIndexRange).call();
	}

}
