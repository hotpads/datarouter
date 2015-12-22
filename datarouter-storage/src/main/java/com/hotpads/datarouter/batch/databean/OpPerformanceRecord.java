package com.hotpads.datarouter.batch.databean;

import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;

public class OpPerformanceRecord extends BaseDatabean<OpPerformanceRecordKey,OpPerformanceRecord>{

	private OpPerformanceRecordKey key;
	private Integer batchSize;
	private Long rowCount;
	private Long timeSpent;

	public static class F{
		public static String
			nodeName = "nodeName",
			opName = "opName",
			timestamp = "timestamp",
			nanotime = "nanotime",
			batchSize = "batchSize",
			rowCount = "rowCount",
			timeSpent = "timeSpent"
			;
	}

	public static class OpPerformanceRecordFielder
	extends BaseDatabeanFielder<OpPerformanceRecordKey,OpPerformanceRecord>{

		@Override
		public Class<? extends Fielder<OpPerformanceRecordKey>> getKeyFielderClass(){
			return OpPerformanceRecordKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(OpPerformanceRecord databean){
			return FieldTool.createList(
					new IntegerField(F.batchSize, databean.batchSize),
					new LongField(F.rowCount, databean.rowCount),
					new LongField(F.timeSpent, databean.timeSpent));
		}

	}

	public OpPerformanceRecord(){
		this.key = new OpPerformanceRecordKey();
	}

	public OpPerformanceRecord(String opName, Integer batchSize, Long rowCount, Long timeSpent){
		this.key = new OpPerformanceRecordKey(opName, System.currentTimeMillis(), System.nanoTime());
		this.batchSize = batchSize;
		this.rowCount = rowCount;
		this.timeSpent = timeSpent;
	}

	@Override
	public Class<OpPerformanceRecordKey> getKeyClass(){
		return OpPerformanceRecordKey.class;
	}

	@Override
	public OpPerformanceRecordKey getKey(){
		return key;
	}

	public String getOpName(){
		return key.getOpName();
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