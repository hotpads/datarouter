package com.hotpads.datarouter.client.imp.jdbc.node.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetByIndexOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetIndexOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.util.core.CollectionTool;

public class JdbcFullyManagedUniqueIndexNode<PK extends PrimaryKey<PK>,
											D extends Databean<PK, D>,
											IK extends PrimaryKey<IK>, 
											IE extends UniqueIndexEntry<IK, IE, PK, D>,
											IF extends DatabeanFielder<IK,IE>>
		implements ManagedUniqueIndexNode<PK, D, IK, IE>{

	private Class<IE> indexEntryClass;
	private Class<IF> indexFielderClass;
	private PhysicalMapStorageNode<PK, D> mainNode;

	@Override
	public D lookupUnique(IK indexKey, Config config){
		return CollectionTool.getFirst(lookupMultiUnique(Collections.singleton(indexKey), config));
	}

	@Override
	public List<D> lookupMultiUnique(Collection<IK> uniqueKeys, Config config){
		BaseJdbcOp<List<D>> op = new JdbcGetByIndexOp<>(mainNode, uniqueKeys, false, config);
		return new SessionExecutorImpl<List<D>>(op, "fullymanagedIndexLookupMultiUnique").call();
	}
	
	@Override
	public IE lookupUniqueIndex(IK uniqueKey, Config config){
		return CollectionTool.getFirst(lookupMultiUniqueIndex(Collections.singleton(uniqueKey), config));
	}

	@Override
	public List<IE> lookupMultiUniqueIndex(Collection<IK> uniqueKeys, Config config){
		BaseJdbcOp<List<IE>> op = new JdbcGetIndexOp<>(mainNode, config, indexEntryClass, indexFielderClass, uniqueKeys);
		return new SessionExecutorImpl<List<IE>>(op, "fullyManagedIndexLookupMultiIndexUnique").call();
	}

	@Override
	public void deleteUnique(IK indexKey, Config config){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteMultiUnique(Collection<IK> uniqueKeys, Config config){
		// TODO Auto-generated method stub
		
	}

}
