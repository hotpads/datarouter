package com.hotpads.datarouter.client.imp.jdbc.node.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetIndexOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntryTool;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;

public class JdbcManagedMultiIndexNode<PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>,
	IE extends MultiIndexEntry<IK, IE, PK, D>, IF extends DatabeanFielder<IK, IE>>
		implements ManagedMultiIndexNode<PK, D, IK, IE>{

	private PhysicalMapStorageNode<PK, D> mainNode;
	private Class<IF> indexFielderClass;
	private Class<IE> indexEntryClass;

	public JdbcManagedMultiIndexNode(PhysicalMapStorageNode<PK, D> backingMapNode, Class<IE> indexEntryClass,
			Class<IF> indexFielder){
		this.mainNode = backingMapNode;
		this.indexFielderClass = indexFielder;
		this.indexEntryClass = indexEntryClass;
	}
	
	@Override
	public List<IE> lookupMultiIndex(IK indexKey, boolean wildcardLastField, Config config){
		return lookupMultiIndexMulti(Collections.singleton(indexKey), wildcardLastField, config);
	}
	
	@Override
	public List<IE> lookupMultiIndexMulti(Collection<IK> indexKeys, boolean wildcardLastField, Config config){
		BaseJdbcOp<List<IE>> op = new JdbcGetIndexOp<>(mainNode, config, indexEntryClass, indexFielderClass, indexKeys);
		return new SessionExecutorImpl<List<IE>>(op, "managedIndexLookupMultiIndexMulti").call();
	}

	@Override
	public List<D> lookupMulti(IK indexKey, boolean wildcardLastField, Config config){
		return lookupMultiMulti(Collections.singleton(indexKey), wildcardLastField, config);
	}

	@Override
	public List<D> lookupMultiMulti(Collection<IK> indexKeys, boolean wildcardLastField, Config config){
		List<IE> entries = lookupMultiIndexMulti(indexKeys, wildcardLastField, config);
		List<PK> targetKeys = IndexEntryTool.getPrimaryKeys(entries);
		return mainNode.getMulti(targetKeys, config);
	}

}
