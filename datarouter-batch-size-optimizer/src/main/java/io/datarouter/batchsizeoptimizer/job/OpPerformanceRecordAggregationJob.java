/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.batchsizeoptimizer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.datarouter.batchsizeoptimizer.storage.performancerecord.DatarouterOpPerformanceRecordDao;
import io.datarouter.batchsizeoptimizer.storage.performancerecord.OpPerformanceRecord;
import io.datarouter.batchsizeoptimizer.storage.performancerecord.OpPerformanceRecordKey;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.scanner.Scanner;

public class OpPerformanceRecordAggregationJob extends BaseJob{

	private static final int BATCH_SIZE = 1000;

	@Inject
	private DatarouterOpPerformanceRecordDao opPerformanceRecordDao;

	@Override
	public void run(TaskTracker tracker){
		String currentOpName = null;
		List<OpPerformanceRecordKey> recordsToDelete = new ArrayList<>(BATCH_SIZE);
		Map<Integer,AggregatedRecord> aggregatedRecordsByBatchSize = new HashMap<>();
		for(OpPerformanceRecord record : opPerformanceRecordDao.scan().iterable()){
			if(currentOpName != null && !currentOpName.equals(record.getKey().getOpName())){
				saveAggregatedRecord(aggregatedRecordsByBatchSize);
				aggregatedRecordsByBatchSize.clear();
			}
			currentOpName = record.getKey().getOpName();
			aggregatedRecordsByBatchSize.computeIfAbsent(
					record.getBatchSize(),
					$ -> new AggregatedRecord(record.getKey().getOpName(), record.getBatchSize())).addRecord(record);
			recordsToDelete.add(record.getKey());
			if(recordsToDelete.size() > BATCH_SIZE){
				opPerformanceRecordDao.deleteMulti(new ArrayList<>(recordsToDelete));
				recordsToDelete.clear();
			}
		}
		saveAggregatedRecord(aggregatedRecordsByBatchSize);
		opPerformanceRecordDao.deleteMulti(recordsToDelete);
	}

	private void saveAggregatedRecord(Map<Integer,AggregatedRecord> aggregatedRecordsByBatchSize){
		Scanner.of(aggregatedRecordsByBatchSize.values())
				.map(AggregatedRecord::buildOpPerformanceRecord)
				.flush(opPerformanceRecordDao::putMulti);
	}

	private static class AggregatedRecord{

		private String opName;
		private Integer batchSize;
		private long timeSpent;
		private long rowCount;

		private AggregatedRecord(String opName, Integer batchSize){
			this.opName = opName;
			this.batchSize = batchSize;
			this.timeSpent = 0;
			this.rowCount = 0;
		}

		private void addRecord(OpPerformanceRecord record){
			timeSpent += record.getTimeSpent();
			rowCount += record.getRowCount();
		}

		private OpPerformanceRecord buildOpPerformanceRecord(){
			return new OpPerformanceRecord(opName, batchSize, rowCount, timeSpent);
		}

	}

}
