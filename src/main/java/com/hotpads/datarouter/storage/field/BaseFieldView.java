package com.hotpads.datarouter.storage.field;

public class BaseFieldView implements FieldView {

	@Override
	public String getViewColumnName(String columnName) {
		return columnName;
	}

	@Override
	public String getViewValue(String value) {
		return value;
	}
}
