package com.hotpads.datarouter.client.imp.jdbc.node.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcGetIndexOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntryTool;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class JdbcManagedUniqueIndexNode
		<PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>, 
		IE extends UniqueIndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseJdbcManagedIndexNode<PK, D, IK, IE, IF>
implements ManagedUniqueIndexNode<PK, D, IK, IE, IF>{
	
	public JdbcManagedUniqueIndexNode(PhysicalMapStorageNode<PK, D> node, JdbcFieldCodecFactory fieldCodecFactory, 
			NodeParams<IK, IE, IF> params, String name){
		super(node, fieldCodecFactory, params, name);
	}

	@Override
	public List<IE> getMulti(Collection<IK> uniqueKeys, final Config config){
		String opName = ManagedUniqueIndexNode.OP_lookupMultiUniqueIndex;
		BaseJdbcOp<List<IE>> op = new JdbcGetIndexOp<>(node, config, fieldInfo.getDatabeanClass(),
				fieldInfo.getFielderClass(), uniqueKeys);
		return new SessionExecutorImpl<List<IE>>(op, opName).call();
	}
	
	@Override
	public IE get(IK uniqueKey, Config config){
		return DrCollectionTool.getFirst(getMulti(Collections.singleton(uniqueKey), config));
	}

	@Override
	public D lookupUnique(IK uniqueKey, Config config){
		IE indexEntry = get(uniqueKey, config);
		if(indexEntry == null){
			return null;
		}
		return node.get(indexEntry.getTargetKey(), config);
	}
	
	@Override
	public List<D> lookupMultiUnique(Collection<IK> fromListingKeys, final Config config){
		List<IE> indexEntries = getMulti(fromListingKeys, config);
		List<PK> targetKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		return node.getMulti(targetKeys, config);
	}

	@Override
	public void deleteUnique(IK uniqueKey, Config config){
		IE indexEntry = get(uniqueKey, config);
		if(indexEntry == null){
			return;
		}
		node.delete(indexEntry.getTargetKey(), config);
	}

	@Override
	public void deleteMultiUnique(Collection<IK> viewIndexKeys, final Config config){
		List<IE> indexEntries = getMulti(viewIndexKeys, config);
		List<PK> targetKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		node.deleteMulti(targetKeys, config);
	}

}
