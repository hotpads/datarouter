package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcIndexScanOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Configs;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchingSortedScanner;

public class JdbcIndexScanner<PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>, L extends Lookup<PK>>
		extends BaseBatchingSortedScanner<D, D>{

	private static final Integer BATCH_SIZE = 1000;
	
	private JdbcReaderNode<PK, D, F> node;
	private Class<L> indexClass;
	private boolean retreiveAllFields;
	private String traceName;

	public JdbcIndexScanner(JdbcReaderNode<PK, D, F> node, Class<L> indexClass, boolean retreiveAllFields, String traceName){
		this.node = node;
		this.indexClass = indexClass;
		this.retreiveAllFields = retreiveAllFields;
		this.traceName = traceName;
	}

	@Override
	protected void loadNextBatch(){
		currentBatchIndex = 0;
		L lastRowOfPreviousBatch;
		try{
			lastRowOfPreviousBatch = indexClass.newInstance();
		}catch (InstantiationException | IllegalAccessException e){
			throw new RuntimeException(indexClass.getCanonicalName() + " must have a no-arg constructor", e);
		}
		boolean isStartInclusive = true;
		if (currentBatch != null){
			D endOfLastBatch = CollectionTool.getLast(currentBatch);
			if (endOfLastBatch == null){
				currentBatch = null;
				return;
			}
			try{
				lastRowOfPreviousBatch = indexClass.getDeclaredConstructor(endOfLastBatch.getClass()).newInstance(
						endOfLastBatch);
			}catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e){
				throw new RuntimeException(indexClass.getCanonicalName() + " must have a constructor with "
						+ endOfLastBatch.getClass().getCanonicalName() + " as the unique parameter", e);
			}
			isStartInclusive = false;
		}
		Range<L> range = new Range<L>(lastRowOfPreviousBatch, isStartInclusive);

		currentBatch = doLoad(range);
		if (CollectionTool.size(currentBatch) < BATCH_SIZE_DEFAULT){
			noMoreBatches = true;
		}
	}

	@Override
	protected void setCurrentFromResult(D result){
		this.current = result;
	}

	private List<D> doLoad(Range<L> start){
		Config config = Configs.slaveOk().setLimit(BATCH_SIZE);
		JdbcIndexScanOp<PK, D, F, L> op = new JdbcIndexScanOp<PK, D, F, L>(node, start, indexClass, config,
				retreiveAllFields, traceName);
		return new SessionExecutorImpl<List<D>>(op, traceName).call();
	}
	
}
