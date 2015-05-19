package com.hotpads.datarouter.client.imp.jdbc.node.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcGetByIndexOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcGetIndexOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcDeleteByIndexOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.index.UniqueIndexReader;
import com.hotpads.datarouter.node.op.index.UniqueIndexWriter;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class JdbcTxnManagedUniqueIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>, 
		IE extends UniqueIndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseJdbcManagedIndexNode<PK,D,IK,IE,IF>
implements ManagedUniqueIndexNode<PK, D, IK, IE, IF>{

	private final JdbcFieldCodecFactory fieldCodecFactory;
	
	public JdbcTxnManagedUniqueIndexNode(PhysicalMapStorageNode<PK, D> node, JdbcFieldCodecFactory fieldCodecFactory, 
			NodeParams<IK, IE, IF> params, String name){
		super(node, fieldCodecFactory, params, name);
		this.fieldCodecFactory = fieldCodecFactory;
	}

	@Override
	public D lookupUnique(IK indexKey, Config config){
		return DrCollectionTool.getFirst(lookupMultiUnique(Collections.singleton(indexKey), config));
	}

	@Override
	public List<D> lookupMultiUnique(Collection<IK> uniqueKeys, Config config){
		String opName = UniqueIndexReader.OP_lookupMultiUnique;
		BaseJdbcOp<List<D>> op = new JdbcGetByIndexOp<>(node, fieldCodecFactory, uniqueKeys, false, config);
		return new SessionExecutorImpl<List<D>>(op, opName).call();
	}
	
	@Override
	public IE get(IK uniqueKey, Config config){
		return DrCollectionTool.getFirst(getMulti(Collections.singleton(uniqueKey), config));
	}

	@Override
	public List<IE> getMulti(Collection<IK> uniqueKeys, Config config){
		String opName = ManagedUniqueIndexNode.OP_lookupMultiUniqueIndex;
		BaseJdbcOp<List<IE>> op = new JdbcGetIndexOp<>(node, fieldCodecFactory, config, fieldInfo.getDatabeanClass(),
				fieldInfo.getFielderClass(), uniqueKeys);
		return new SessionExecutorImpl<List<IE>>(op, opName).call();
	}

	@Override
	public void deleteUnique(IK indexKey, Config config){
		deleteMultiUnique(Collections.singleton(indexKey), config);
	}

	@Override
	public void deleteMultiUnique(Collection<IK> uniqueKeys, Config config){
		String opName = UniqueIndexWriter.OP_deleteMultiUnique;
		BaseJdbcOp<Long> op = new JdbcDeleteByIndexOp<>(node, fieldCodecFactory, uniqueKeys, config);
		new SessionExecutorImpl<Long>(op, opName).call();
	}

}
