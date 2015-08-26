package com.hotpads.datarouter.client.imp.jdbc.node.mixin;

import com.hotpads.datarouter.client.imp.jdbc.execution.JdbcOpRetryTool;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcPrefixDeleteOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.PhysicalSortedStorageWriterNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface JdbcSortedStorageWriterMixin<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends PhysicalSortedStorageWriterNode<PK,D>, JdbcStorageMixin{

	@Override
	public default void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config){
		String opName = SortedStorageWriter.OP_deleteRangeWithPrefix;
		JdbcPrefixDeleteOp<PK,D> op = new JdbcPrefixDeleteOp<>(this, getFieldCodecFactory(), prefix, wildcardLastField,
				config);
		JdbcOpRetryTool.tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

}
