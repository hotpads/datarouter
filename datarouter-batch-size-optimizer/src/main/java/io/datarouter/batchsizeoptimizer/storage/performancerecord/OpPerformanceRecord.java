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
package io.datarouter.batchsizeoptimizer.storage.performancerecord;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class OpPerformanceRecord extends BaseDatabean<OpPerformanceRecordKey,OpPerformanceRecord>{

	private Integer batchSize;
	private Long rowCount;
	private Long timeSpent;

	public static class FieldKeys{
		public static final IntegerFieldKey batchSize = new IntegerFieldKey("batchSize");
		public static final LongFieldKey rowCount = new LongFieldKey("rowCount");
		public static final LongFieldKey timeSpent = new LongFieldKey("timeSpent");
	}

	public static class OpPerformanceRecordFielder
	extends BaseDatabeanFielder<OpPerformanceRecordKey,OpPerformanceRecord>{

		public OpPerformanceRecordFielder(){
			super(OpPerformanceRecordKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(OpPerformanceRecord databean){
			return List.of(
					new IntegerField(FieldKeys.batchSize, databean.batchSize),
					new LongField(FieldKeys.rowCount, databean.rowCount),
					new LongField(FieldKeys.timeSpent, databean.timeSpent));
		}

	}

	public OpPerformanceRecord(){
		super(new OpPerformanceRecordKey());
	}

	public OpPerformanceRecord(String opName, Integer batchSize, Long rowCount, Long timeSpent){
		super(new OpPerformanceRecordKey(opName, System.currentTimeMillis(), System.nanoTime()));
		this.batchSize = batchSize;
		this.rowCount = rowCount;
		this.timeSpent = timeSpent;
	}

	@Override
	public Supplier<OpPerformanceRecordKey> getKeySupplier(){
		return OpPerformanceRecordKey::new;
	}

	public Integer getBatchSize(){
		return batchSize;
	}

	public Long getTimeSpent(){
		return timeSpent;
	}

	public Long getRowCount(){
		return rowCount;
	}

	public Long getRowsPerSeconds(){
		if(timeSpent == 0){
			return 0L;
		}
		return 1000 * rowCount / timeSpent;
	}

}
