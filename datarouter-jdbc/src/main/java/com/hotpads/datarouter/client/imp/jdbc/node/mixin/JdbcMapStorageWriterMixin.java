package com.hotpads.datarouter.client.imp.jdbc.node.mixin;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.jdbc.execution.JdbcOpRetryTool;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcDeleteAllOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcDeleteOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.PhysicalMapStorageWriterNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;

public interface JdbcMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends PhysicalMapStorageWriterNode<PK,D>, JdbcStorageMixin{

	@Override
	public default void deleteAll(final Config config) {
		String opName = MapStorageWriter.OP_deleteAll;
		JdbcDeleteAllOp<PK,D> op = new JdbcDeleteAllOp<>(this, config);
		JdbcOpRetryTool.tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

	@Override
	public default void delete(PK key, Config config){
		String opName = MapStorageWriter.OP_delete;
		JdbcDeleteOp<PK,D> op = new JdbcDeleteOp<>(this, getFieldCodecFactory(), DrListTool.wrap(key), config);
		JdbcOpRetryTool.tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

	@Override
	public default void deleteMulti(final Collection<PK> keys, final Config config){
		String opName = MapStorageWriter.OP_deleteMulti;
		if(DrCollectionTool.isEmpty(keys)){
			return;//avoid starting txn
		}
		JdbcDeleteOp<PK,D> op = new JdbcDeleteOp<>(this, getFieldCodecFactory(), keys, config);
		JdbcOpRetryTool.tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

}
