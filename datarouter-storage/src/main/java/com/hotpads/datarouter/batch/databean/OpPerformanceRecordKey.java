package com.hotpads.datarouter.batch.databean;

import java.util.List;

import com.hotpads.datarouter.batch.databean.OpPerformanceRecord.F;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class OpPerformanceRecordKey extends BasePrimaryKey<OpPerformanceRecordKey>{

	private String opName;
	private Long timestamp;
	private Long nanotime;

	public OpPerformanceRecordKey(String opName, Long timestamp, Long nanotime){
		this.opName = opName;
		this.timestamp = timestamp;
		this.nanotime = nanotime;
	}

	public OpPerformanceRecordKey(){

	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(F.opName, opName, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new LongField(F.timestamp, timestamp),
				new LongField(F.nanotime, nanotime));
	}

	public Long getTimestamp(){
		return timestamp;
	}

	public String getOpName(){
		return opName;
	}

}
