package com.hotpads.datarouter.client.imp.jdbc.node.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetIndexOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntryTool;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.util.core.CollectionTool;

public class JdbcManagedUniqueIndexNode<PK extends PrimaryKey<PK>,
									D extends Databean<PK, D>,
									IK extends PrimaryKey<IK>, 
									IE extends UniqueIndexEntry<IK, IE, PK, D>,
									IF extends DatabeanFielder<IK,IE>>
		implements ManagedUniqueIndexNode<PK, D, IK, IE>{
	
	private Class<IE> indexEntryClass;
	private Class<IF> indexFielderClass;
	private PhysicalMapStorageNode<PK, D> mainNode;

	public JdbcManagedUniqueIndexNode(PhysicalMapStorageNode<PK, D> mainNode, Class<IE> indexEntryClass, Class<IF> indexFielderClass){
		this.indexFielderClass = indexFielderClass;
		this.indexEntryClass = indexEntryClass;
		this.mainNode = mainNode;
	}
	
	@Override
	public List<IE> lookupMultiUniqueIndex(Collection<IK> uniqueKeys, final Config config){
		BaseJdbcOp<List<IE>> op = new JdbcGetIndexOp<>(mainNode, config, indexEntryClass, indexFielderClass, uniqueKeys);
		return new SessionExecutorImpl<List<IE>>(op, "managedIndexLookupMultiIndexUnique").call();
	}
	
	@Override
	public IE lookupUniqueIndex(IK uniqueKey, Config config){
		return CollectionTool.getFirst(lookupMultiUniqueIndex(Collections.singleton(uniqueKey), config));
	}

	@Override
	public D lookupUnique(IK uniqueKey, Config config){
		IE indexEntry = lookupUniqueIndex(uniqueKey, config);
		if(indexEntry == null){
			return null;
		}
		return mainNode.get(indexEntry.getTargetKey(), config);
	}
	
	@Override
	public List<D> lookupMultiUnique(Collection<IK> fromListingKeys, final Config config){
		List<IE> indexEntries = lookupMultiUniqueIndex(fromListingKeys, config);
		List<PK> targetKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		return mainNode.getMulti(targetKeys, config);
	}

	@Override
	public void deleteUnique(IK uniqueKey, Config config){
		IE indexEntry = lookupUniqueIndex(uniqueKey, config);
		if(indexEntry == null){
			return;
		}
		mainNode.delete(indexEntry.getTargetKey(), config);
	}

	@Override
	public void deleteMultiUnique(Collection<IK> viewIndexKeys, final Config config){
		List<IE> indexEntries = lookupMultiUniqueIndex(viewIndexKeys, config);
		List<PK> targetKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		mainNode.deleteMulti(targetKeys, config);
	}

}
