package com.hotpads.datarouter.client.imp.jdbc.node;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcRollbackRetryingCallable;
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
import com.hotpads.util.core.concurrent.RetryableTool;

public class JdbcNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends JdbcReaderNode<PK,D,F>
implements PhysicalIndexedSortedMapStorageNode<PK,D>, JdbcIndexedStorageWriterMixin<PK,D>{
	
	private static final int NUM_ROLLBACK_ATTEMPTS = 5;
	private static final long ROLLBACK_BACKOFF_MS = 4;
	
	//this defaults to 1, so you must explicitly call config.setNumAttempts(x) to get retries on 
	// non-MySQLTransactionRollbackExceptions
	private static final int DEFAULT_NUM_ATTEMPTS = 1;
	private static final long DEFAULT_BACKOFF_MS = 1;

	
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
		tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}


	@Override
	public void putMulti(Collection<D> databeans, final Config config) {
		String opName = MapStorageWriter.OP_putMulti;
		if(DrCollectionTool.isEmpty(databeans)){
			return;//avoid starting txn
		}
		JdbcPutOp<PK,D,F> op = new JdbcPutOp<>(this, fieldCodecFactory, databeans, config);
		tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

	@Override
	public void deleteAll(final Config config) {
		String opName = MapStorageWriter.OP_deleteAll;
		JdbcDeleteAllOp<PK,D,F> op = new JdbcDeleteAllOp<>(this, config);
		tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

	@Override
	public void delete(PK key, Config config){
		String opName = MapStorageWriter.OP_delete;
		JdbcDeleteOp<PK,D> op = new JdbcDeleteOp<>(this, fieldCodecFactory, DrListTool.wrap(key), config);
		tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config){
		String opName = MapStorageWriter.OP_deleteMulti;
		if(DrCollectionTool.isEmpty(keys)){
			return;//avoid starting txn
		}
		JdbcDeleteOp<PK,D> op = new JdbcDeleteOp<>(this, fieldCodecFactory, keys, config);
		tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}

	/************************************ SortedStorageWriter methods ****************************/

	@Override
	public void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		String opName = SortedStorageWriter.OP_deleteRangeWithPrefix;
		JdbcPrefixDeleteOp<PK,D> op = new JdbcPrefixDeleteOp<>(this, fieldCodecFactory, prefix, wildcardLastField,
				config);
		tryNTimes(new SessionExecutorImpl<>(op, getTraceName(opName)), config);
	}
	
	
	/********************************** private **************************************************/
	
	/* This executes the query config.getNumAttempts() times. Then for each attempt, it will retry rollbacks a fixed
	 * number of times (NUM_ROLLBACK_ATTEMPTS - 1). If config.getNumAttempts() is 2 and NUM_ROLLBACK_ATTEMPTS is 3, then
	 * we may start 6 txns */
	private <T> T tryNTimes(SessionExecutorImpl<T> opCallable, Config config){
		config = Config.nullSafe(config);
		JdbcRollbackRetryingCallable<T> retryingCallable = new JdbcRollbackRetryingCallable<>(opCallable,
				NUM_ROLLBACK_ATTEMPTS, ROLLBACK_BACKOFF_MS);
		int numAttempts = config.getNumAttemptsOrUse(DEFAULT_NUM_ATTEMPTS);
		return RetryableTool.tryNTimesWithBackoffUnchecked(retryingCallable, numAttempts, DEFAULT_BACKOFF_MS);
	}
}
