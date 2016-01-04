package com.hotpads.datarouter.batch;

import com.hotpads.datarouter.batch.databean.OpOptimizedBatchSize;
import com.hotpads.datarouter.batch.databean.OpOptimizedBatchSizeKey;
import com.hotpads.datarouter.batch.databean.OpPerformanceRecord;
import com.hotpads.datarouter.batch.databean.OpPerformanceRecordKey;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public interface BatchSizeOptimizerNodes{

	SortedMapStorage<OpPerformanceRecordKey,OpPerformanceRecord> getOpPerformanceRecord();
	SortedMapStorage<OpOptimizedBatchSizeKey,OpOptimizedBatchSize> getOpOptimizedBatchSize();
}
