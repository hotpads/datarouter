package com.hotpads.handler.exception;

import static com.hotpads.handler.exception.ExceptionRecord.COL_id;
import static com.hotpads.handler.exception.ExceptionRecord.LENGTH_id;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class ExceptionRecordKey extends BasePrimaryKey<ExceptionRecordKey> {

	private String id;
	
	ExceptionRecordKey() {
		
	}
	
	public ExceptionRecordKey(String id) {
		this.id = id;
	}

	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(new StringField(COL_id, id, LENGTH_id));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
