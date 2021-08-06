/*
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
package io.datarouter.batchsizeoptimizer.storage.optimizedbatch;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.batchsizeoptimizer.BatchSizeOptimizer;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.DoubleField;
import io.datarouter.model.field.imp.comparable.DoubleFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class OpOptimizedBatchSize extends BaseDatabean<OpOptimizedBatchSizeKey,OpOptimizedBatchSize>{

	private Integer batchSize;
	private Double curiosity;

	public static class FieldKeys{
		public static final IntegerFieldKey batchSize = new IntegerFieldKey("batchSize");
		public static final DoubleFieldKey curiosity = new DoubleFieldKey("curiosity");
	}

	public static class OpOptimizedBatchSizeFielder
	extends BaseDatabeanFielder<OpOptimizedBatchSizeKey,OpOptimizedBatchSize>{

		public OpOptimizedBatchSizeFielder(){
			super(OpOptimizedBatchSizeKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(OpOptimizedBatchSize databean){
			return List.of(
					new IntegerField(FieldKeys.batchSize, databean.batchSize),
					new DoubleField(FieldKeys.curiosity, databean.curiosity));
		}

	}

	public OpOptimizedBatchSize(){
		this(null, null, null);
	}

	public OpOptimizedBatchSize(String opName, Integer batchSize, Double curiosity){
		super(new OpOptimizedBatchSizeKey(opName));
		this.batchSize = batchSize;
		this.curiosity = curiosity;
	}

	@Override
	public Supplier<OpOptimizedBatchSizeKey> getKeySupplier(){
		return OpOptimizedBatchSizeKey::new;
	}

	public Integer getBatchSize(){
		return batchSize;
	}

	public Double getCuriosity(){
		return curiosity;
	}

	public static OpOptimizedBatchSize createDefault(String opName){
		return new OpOptimizedBatchSize(
				opName,
				BatchSizeOptimizer.DEFAULT_BATCH_SIZE,
				BatchSizeOptimizer.DEFAULT_CURIOSITY);
	}

}
