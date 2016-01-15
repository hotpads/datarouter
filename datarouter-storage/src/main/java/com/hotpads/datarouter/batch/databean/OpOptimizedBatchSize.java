package com.hotpads.datarouter.batch.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.batch.BatchSizeOptimizer;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;

public class OpOptimizedBatchSize extends BaseDatabean<OpOptimizedBatchSizeKey,OpOptimizedBatchSize>{

	private OpOptimizedBatchSizeKey key;
	private Integer batchSize;
	private Double curiosity;

	public static class F{
		public static String
				opName = "opName",
				batchSize = "batchSize",
				curiosity = "curiosity";
	}

	public static class OpOptimizedBatchSizeFielder
	extends BaseDatabeanFielder<OpOptimizedBatchSizeKey,OpOptimizedBatchSize>{

		@Override
		public Class<? extends Fielder<OpOptimizedBatchSizeKey>> getKeyFielderClass(){
			return OpOptimizedBatchSizeKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(OpOptimizedBatchSize databean){
			return Arrays.asList(
					new IntegerField(F.batchSize, databean.batchSize),
					new DumbDoubleField(F.curiosity, databean.curiosity));
		}

	}

	//constructors

	public OpOptimizedBatchSize(){
		this(null, null, null);
	}

	public OpOptimizedBatchSize(String opName, Integer batchSize, Double curiosity){
		this.key = new OpOptimizedBatchSizeKey(opName);
		this.batchSize = batchSize;
		this.curiosity = curiosity;
	}

	//databean methods

	@Override
	public Class<OpOptimizedBatchSizeKey> getKeyClass(){
		return OpOptimizedBatchSizeKey.class;
	}

	@Override
	public OpOptimizedBatchSizeKey getKey(){
		return key;
	}

	public Integer getBatchSize(){
		return batchSize;
	}

	public Double getCuriosity(){
		return curiosity;
	}

	public static OpOptimizedBatchSize createDefault(String opName){
		return new OpOptimizedBatchSize(opName, BatchSizeOptimizer.DEFAULT_BATCH_SIZE,
				BatchSizeOptimizer.DEFAULT_CURIOSITY);
	}

}
