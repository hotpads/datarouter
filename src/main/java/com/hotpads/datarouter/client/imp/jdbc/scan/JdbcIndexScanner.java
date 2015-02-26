package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcIndexScanOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Configs;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.CollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchingSortedScanner;
import com.hotpads.util.core.java.ReflectionTool;

public class JdbcIndexScanner<PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>, PKLookup extends BaseLookup<PK>>
		extends BaseBatchingSortedScanner<PKLookup,PKLookup>{

	private static final Integer BATCH_SIZE = 1000;
	
	private JdbcReaderNode<PK, D, F> node;
	private Class<PKLookup> indexClass;
	private String traceName;

	public JdbcIndexScanner(JdbcReaderNode<PK, D, F> node, Class<PKLookup> indexClass, String traceName){
		this.node = node;
		this.indexClass = indexClass;
		this.traceName = traceName;
	}

	@Override
	protected void loadNextBatch(){
		currentBatchIndex = 0;
		PKLookup lastRowOfPreviousBatch = ReflectionTool.create(indexClass, indexClass.getCanonicalName() + " must have a no-arg constructor");
		boolean isStartInclusive = true;
		if (currentBatch != null){
			PKLookup endOfLastBatch = CollectionTool.getLast(currentBatch);
			if (endOfLastBatch == null){
				currentBatch = null;
				return;
			}
			lastRowOfPreviousBatch = endOfLastBatch;
			isStartInclusive = false;
		}
		Range<PKLookup> range = new Range<PKLookup>(lastRowOfPreviousBatch, isStartInclusive);

		currentBatch = doLoad(range);
		if (CollectionTool.size(currentBatch) < BATCH_SIZE_DEFAULT){
			noMoreBatches = true;
		}
	}

	@Override
	protected void setCurrentFromResult(PKLookup result){
		this.current = result;
	}

	private List<PKLookup> doLoad(Range<PKLookup> start){
		Config config = Configs.slaveOk().setLimit(BATCH_SIZE);
		JdbcIndexScanOp<PK, D, F, PKLookup> op = new JdbcIndexScanOp<PK, D, F, PKLookup>(node, start, indexClass, config,
				traceName);
		return new SessionExecutorImpl<List<PKLookup>>(op, traceName).call();
	}
	
}
