package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcManagedIndexScanOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Configs;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchingSortedScanner;

public class JdbcManagedIndexScanner<
		PK extends PrimaryKey<PK>, 
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>>
extends BaseBatchingSortedScanner<IE,IE>{
	
	private PhysicalNode<PK, D> node;
	private String traceName;
	private ManagedNode<IK, IE, IF> managedNode;
	private Range<IK> range;

	public JdbcManagedIndexScanner(PhysicalNode<PK, D> node, ManagedNode<IK, IE, IF> managedNode, Range<IK> range,
			String traceName){
		this.node = node;
		this.managedNode = managedNode;
		this.range = range;
		this.traceName = traceName;
	}

	@Override
	protected void loadNextBatch(){
		currentBatchIndex = 0;
		IK lastRowOfPreviousBatch = range.getStart();
		boolean isStartInclusive = range.getStartInclusive();
		if (currentBatch != null){
			IE endOfLastBatch = CollectionTool.getLast(currentBatch);
			if (endOfLastBatch == null){
				currentBatch = null;
				return;
			}
			lastRowOfPreviousBatch = endOfLastBatch.getKey();
			isStartInclusive = false;
		}
		Range<IK> batchRange = new Range<IK>(lastRowOfPreviousBatch, isStartInclusive, range.getEnd(),
				range.getEndInclusive());

		currentBatch = doLoad(batchRange);
		
		if (CollectionTool.size(currentBatch) < BATCH_SIZE_DEFAULT){
			noMoreBatches = true;
		}
	}

	@Override
	protected void setCurrentFromResult(IE result){
		this.current = result;
	}

	private List<IE> doLoad(Range<IK> range){
		Config config = Configs.slaveOk().setLimit(JdbcNode.DEFAULT_ITERATE_BATCH_SIZE);
		JdbcManagedIndexScanOp<PK, D, IK, IE, IF> op = new JdbcManagedIndexScanOp<>(node, managedNode, range,
				config, traceName);
		return new SessionExecutorImpl<List<IE>>(op, traceName).call();
	}
	
}
