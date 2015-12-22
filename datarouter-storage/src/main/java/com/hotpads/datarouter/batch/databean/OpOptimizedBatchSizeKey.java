package com.hotpads.datarouter.batch.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.batch.databean.OpOptimizedBatchSize.F;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class OpOptimizedBatchSizeKey extends BasePrimaryKey<OpOptimizedBatchSizeKey>{

	private String opName;

	public OpOptimizedBatchSizeKey(){
	}

	public OpOptimizedBatchSizeKey(String opName){
		this.opName = opName;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(F.opName, opName, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	public String getOpName(){
		return opName;
	}

}
