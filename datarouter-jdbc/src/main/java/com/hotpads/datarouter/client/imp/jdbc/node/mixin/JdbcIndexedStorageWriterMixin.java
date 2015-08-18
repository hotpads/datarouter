package com.hotpads.datarouter.client.imp.jdbc.node.mixin;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcDeleteByIndexOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcIndexDeleteOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcUniqueIndexDeleteOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.PhysicalIndexedStorageWriterNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;

public interface JdbcIndexedStorageWriterMixin<PK extends PrimaryKey<PK>, D extends Databean<PK, D>>
extends PhysicalIndexedStorageWriterNode<PK,D>, JdbcStorageMixin{

	@Override
	public default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		String opName = IndexedStorageWriter.OP_deleteUnique;
		JdbcUniqueIndexDeleteOp<PK,D> op = new JdbcUniqueIndexDeleteOp<>(this, getFieldCodecFactory(), DrListTool
				.wrap(uniqueKey), config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	@Override
	public default void deleteMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
		String opName = IndexedStorageWriter.OP_deleteMultiUnique;
		if(DrCollectionTool.isEmpty(uniqueKeys)){
			return;//avoid starting txn
		}
		JdbcUniqueIndexDeleteOp<PK,D> op = new JdbcUniqueIndexDeleteOp<>(this, getFieldCodecFactory(), uniqueKeys,
				config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	@Override
	public default void delete(final Lookup<PK> lookup, final Config config) {
		String opName = IndexedStorageWriter.OP_indexDelete;
		JdbcIndexDeleteOp<PK,D> op = new JdbcIndexDeleteOp<>(this, getFieldCodecFactory(), lookup, config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	@Override
	public default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		BaseJdbcOp<Long> op = new JdbcDeleteByIndexOp<>(this, getFieldCodecFactory(), keys, config);
		new SessionExecutorImpl<>(op, IndexedStorageWriter.OP_deleteMultiUnique).call();
	}

}
