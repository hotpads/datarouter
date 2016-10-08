package com.hotpads.datarouter.storage.field;

import java.util.List;

import com.hotpads.datarouter.serialize.fielder.Fielder;

public interface FieldSet<F extends FieldSet<F>> extends Comparable<FieldSet<F>>,Fielder<F>{

	List<Field<?>> getFields();
	List<String> getFieldNames();
	List<?> getFieldValues();
	Object getFieldValue(String fieldName);

	String getPersistentString();
	String getTypedPersistentString();
	void fromPersistentString(String in);

}
