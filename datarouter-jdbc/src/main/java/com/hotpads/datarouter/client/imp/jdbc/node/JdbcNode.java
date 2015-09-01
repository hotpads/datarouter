package com.hotpads.datarouter.client.imp.jdbc.node;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.jdbc.execution.JdbcOpRetryTool;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.mixin.JdbcIndexedStorageWriterMixin;
import com.hotpads.datarouter.client.imp.jdbc.node.mixin.JdbcMapStorageWriterMixin;
import com.hotpads.datarouter.client.imp.jdbc.node.mixin.JdbcSortedStorageWriterMixin;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcPutOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
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
implements PhysicalIndexedSortedMapStorageNode<PK,D>,
		JdbcIndexedStorageWriterMixin<PK,D>,
		JdbcSortedStorageWriterMixin<PK,D>,
		JdbcMapStorageWriterMixin<PK,D>{

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

}
