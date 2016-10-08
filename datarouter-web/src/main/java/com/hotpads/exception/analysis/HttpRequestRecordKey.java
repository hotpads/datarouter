package com.hotpads.exception.analysis;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class HttpRequestRecordKey extends BasePrimaryKey<HttpRequestRecordKey>{

	private String id;

	private static class F {
		public static String id = "id";
	}

	HttpRequestRecordKey() {}

	public HttpRequestRecordKey(String id) {
		this.id = id;
	}

	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(new StringField(F.id, id, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
