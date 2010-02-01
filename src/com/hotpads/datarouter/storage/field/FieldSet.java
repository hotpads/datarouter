package com.hotpads.datarouter.storage.field;

import java.util.List;

public interface FieldSet {

	List<Field> getFields();
	List<String> getFieldNames();
	List<Comparable<?>> getFieldValues();

	Comparable<?> getFieldValue(String fieldName);

	List<String> getSqlValuesEscaped();
	List<String> getSqlNameValuePairsEscaped();
	String getSqlNameValuePairsEscapedConjunction();
}
