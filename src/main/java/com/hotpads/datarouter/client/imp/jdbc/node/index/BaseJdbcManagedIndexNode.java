package com.hotpads.datarouter.client.imp.jdbc.node.index;

import com.hotpads.datarouter.client.imp.jdbc.scan.JdbcManagedIndexScanner;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.base.BaseManagedNode;
import com.hotpads.datarouter.op.scan.ManagedIndexDatabeanScanner;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class BaseJdbcManagedIndexNode
		<PK extends PrimaryKey<PK>, 
		D extends Databean<PK, D>, 
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>, 
		IF extends DatabeanFielder<IK, IE>>
extends BaseManagedNode<PK, D, IK, IE, IF>{

	public BaseJdbcManagedIndexNode(PhysicalMapStorageNode<PK, D> node, NodeParams<IK, IE, IF> params, String name){
		super(node, params, name);
	}

	public SortedScannerIterable<IE> scan(Range<IK> range, Config config){
		String opName = ManagedMultiIndexNode.OP_scanIndex;
		return new SortedScannerIterable<IE>(new JdbcManagedIndexScanner<PK, D, IK, IE, IF>(node, this, range, opName,
				config));
	}

	public SortedScannerIterable<D> scanDatabeans(Range<IK> range, Config config){
		return new SortedScannerIterable<D>(new ManagedIndexDatabeanScanner<>(node, scan(range, config), config));
	}
	
	public SortedScannerIterable<IK> scanKeys(Range<IK> range, Config config){
		//TODO Write something similar to JdbcManagedIndexScanner to scan only keys
		throw new NotImplementedException();
	}
	
}