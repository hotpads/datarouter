package com.hotpads.datarouter.storage.field;

import java.util.List;

public interface Fields {

	List<Field> getFields();
	List<String> getFieldNames();
	List<Comparable<?>> getFieldValues();

	Comparable<?> getFieldValue(String fieldName);

	List<String> getSqlValuesEscaped();
	List<String> getSqlNameValuePairsEscaped();
	String getSqlNameValuePairsEscapedConjunction();
}
