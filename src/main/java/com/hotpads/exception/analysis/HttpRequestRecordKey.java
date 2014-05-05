package com.hotpads.exception.analysis;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.exception.analysis.HttpRequestRecord.F;

@SuppressWarnings("serial")
public class HttpRequestRecordKey extends BasePrimaryKey<HttpRequestRecordKey>{

	private static int
		LENGTH_exceptionRecordId = MySqlColumnType.MAX_LENGTH_VARCHAR;

	private String exceptionRecordId;

	HttpRequestRecordKey() {}

	public HttpRequestRecordKey(String id) {
		this.exceptionRecordId = id;
	}

	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(new StringField(F.exceptionRecordId, exceptionRecordId, LENGTH_exceptionRecordId));
	}

	public String getExceptionRecordId() {
		return exceptionRecordId;
	}

	public void setExceptionRecordId(String exceptionRecordId) {
		this.exceptionRecordId = exceptionRecordId;
	}

}
