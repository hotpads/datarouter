package com.hotpads.datarouter.storage.field;

public interface FieldView {
	String getViewColumnName(String columnName);

	String getViewValue(String value);
}
