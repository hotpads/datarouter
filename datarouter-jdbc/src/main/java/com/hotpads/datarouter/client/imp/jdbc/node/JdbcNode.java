package com.hotpads.datarouter.client.imp.jdbc.node;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.jdbc.execution.JdbcOpRetryTool;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcDeleteAllOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcDeleteOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcPrefixDeleteOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcPutOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class JdbcNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends JdbcReaderNode<PK,D,F>
implements PhysicalIndexedSortedMapStorageNode<PK,D>, JdbcIndexedStorageWriterMixin<PK,D>{
	
	private final JdbcFieldCodecFactory fieldCodecFactory;

	
	public JdbcNode(NodeParams<PK,D,F> params, JdbcFieldCodecFactory fieldCodecFactory){
		super(params, fieldCodecFactory);
		this.fieldCodecFactory = fieldCodecFactory;
	}

	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}

	@Override
	public JdbcFieldCodecFactory getFieldCodecFactory(){
		return fieldCodecFactory;
	}

	/************************************ MapStorageWriter methods ****************************/

	@Override
	public void put(final D databean, final Config config) {
		String opName = MapStorageWriter.OP_put;
		JdbcPutOp<PK,D,F> op = new JdbcPutOp<>(this, fieldCodecFactory, DrListTool.wrap(databean), config);
		JdbcOpRetryTool.tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}


	@Override
	public void putMulti(Collection<D> databeans, final Config config) {
		String opName = MapStorageWriter.OP_putMulti;
		if(DrCollectionTool.isEmpty(databeans)){
			return;//avoid starting txn
		}
		JdbcPutOp<PK,D,F> op = new JdbcPutOp<>(this, fieldCodecFactory, databeans, config);
		JdbcOpRetryTool.tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

	@Override
	public void deleteAll(final Config config) {
		String opName = MapStorageWriter.OP_deleteAll;
		JdbcDeleteAllOp<PK,D,F> op = new JdbcDeleteAllOp<>(this, config);
		JdbcOpRetryTool.tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

	@Override
	public void delete(PK key, Config config){
		String opName = MapStorageWriter.OP_delete;
		JdbcDeleteOp<PK,D> op = new JdbcDeleteOp<>(this, fieldCodecFactory, DrListTool.wrap(key), config);
		JdbcOpRetryTool.tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config){
		String opName = MapStorageWriter.OP_deleteMulti;
		if(DrCollectionTool.isEmpty(keys)){
			return;//avoid starting txn
		}
		JdbcDeleteOp<PK,D> op = new JdbcDeleteOp<>(this, fieldCodecFactory, keys, config);
		JdbcOpRetryTool.tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

	/************************************ SortedStorageWriter methods ****************************/

	@Override
	public void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		String opName = SortedStorageWriter.OP_deleteRangeWithPrefix;
		JdbcPrefixDeleteOp<PK,D> op = new JdbcPrefixDeleteOp<>(this, fieldCodecFactory, prefix, wildcardLastField,
				config);
		JdbcOpRetryTool.tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}
}
