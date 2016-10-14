package com.hotpads.handler.exception;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class ExceptionRecordKey extends BasePrimaryKey<ExceptionRecordKey> {

	private String id;

	public static class FieldKeys{
		public static final StringFieldKey id = new StringFieldKey("id");
	}

	public ExceptionRecordKey() {}

	public ExceptionRecordKey(String id) {
		this.id = id;
	}

	@Override
	public List<Field<?>> getFields() {
		return Arrays.asList(new StringField(FieldKeys.id, id));
	}

	public String getId() {
		return id;
	}

}
