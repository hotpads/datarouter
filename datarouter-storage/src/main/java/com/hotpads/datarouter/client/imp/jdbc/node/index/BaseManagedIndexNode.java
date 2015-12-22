package com.hotpads.datarouter.client.imp.jdbc.node.index;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage;
import com.hotpads.datarouter.node.type.index.base.BaseManagedNode;
import com.hotpads.datarouter.op.scan.ManagedIndexDatabeanScanner;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SingleUseScannerIterable;

public class BaseManagedIndexNode
		<PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>>
extends BaseManagedNode<PK, D, IK, IE, IF>{


	public BaseManagedIndexNode(IndexedMapStorage<PK, D> node, NodeParams<IK, IE, IF> params, String name){
		super(node, params, name);
	}

	public Iterable<IE> scan(Range<IK> range, Config config){
		return node.scanIndex(fieldInfo, range, config);
	}

	public Iterable<IK> scanKeys(Range<IK> range, Config config){
		return node.scanIndexKeys(fieldInfo, range, config);
	}

	public Iterable<D> scanDatabeans(Range<IK> range, Config config){
		return new SingleUseScannerIterable<>(new ManagedIndexDatabeanScanner<>(node, scan(range, config), config));
	}

}
