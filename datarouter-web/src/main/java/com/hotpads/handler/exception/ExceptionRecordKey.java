package com.hotpads.handler.exception;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.handler.exception.ExceptionRecord.F;

@SuppressWarnings("serial")
public class ExceptionRecordKey extends BasePrimaryKey<ExceptionRecordKey> {
	
	public static int
		LENGTH_id = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	private String id;
	
	ExceptionRecordKey() {}
	
	public ExceptionRecordKey(String id) {
		this.id = id;
	}

	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(new StringField(F.id, id, LENGTH_id));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
