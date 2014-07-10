package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetIndexOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.index.UniqueIndexReader;
import com.hotpads.datarouter.node.op.index.UniqueIndexWriter;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class ManagedUniqueIndexNode<PK extends PrimaryKey<PK>,
									D extends Databean<PK, D>,
									F extends DatabeanFielder<PK,D>,
									IK extends PrimaryKey<IK>, 
									IE extends UniqueIndexEntry<IK, IE, PK, D>,
									IF extends DatabeanFielder<IK,IE>>
									implements UniqueIndexReader<PK,D,IK>, UniqueIndexWriter<PK,D,IK>{
	
	private JdbcNode<PK, D, F> mainNode;
	private Class<IE> indexEntryClass;
	private Class<IF> indexFielderClass;

	public ManagedUniqueIndexNode(JdbcNode<PK, D, F> backingMapNode, Class<IF> indexFielderClass, Class<IE> indexEntryClass){
		this.mainNode = backingMapNode;
		this.indexFielderClass = indexFielderClass;
		this.indexEntryClass = indexEntryClass;
	}
	
	public List<IE> lookupMultiIndex(Collection<IK> uniqueKeys, final Config config){
		BaseJdbcOp<List<IE>> op = new JdbcGetIndexOp<>(mainNode, config, indexEntryClass, indexFielderClass, uniqueKeys);
		return new SessionExecutorImpl<List<IE>>(op, "managedIndexLookupMultiIndexUnique").call();
	}
	
	public IE lookupIndex(IK uniqueKey, Config config){
		List<IK> keys = ListTool.create();
		keys.add(uniqueKey);
		return CollectionTool.getFirst(lookupMultiIndex(keys, config));
	}

	@Override
	public D lookupUnique(IK uniqueKey, Config config){
		IE indexEntry = lookupIndex(uniqueKey, config);
		return mainNode.get(indexEntry.getTargetKey(), config);
	}

	@Override
	public void deleteUnique(IK uniqueKey, Config config){
		IE indexEntry = lookupIndex(uniqueKey, config);
		mainNode.delete(indexEntry.getTargetKey(), config);
	}

	@Override
	public void deleteMultiUnique(Collection<IK> viewIndexKeys, final Config config){
		List<IE> indexEntries = lookupMultiIndex(viewIndexKeys, config);
		List<PK> targetKeys = ListTool.create();
		for (IE indexEntry : indexEntries){
			targetKeys.add(indexEntry.getTargetKey());
		}
		mainNode.deleteMulti(targetKeys, config);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<IK> fromListingKeys, final Config config){
		List<IE> indexEntries = lookupMultiIndex(fromListingKeys, config);
		List<PK> targetKeys = ListTool.create();
		for(IE indexEntry : indexEntries){
			targetKeys.add(indexEntry.getTargetKey());
		}
		return mainNode.getMulti(targetKeys, config);
	}

}
