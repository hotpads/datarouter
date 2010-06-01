package com.hotpads.datarouter.storage.field;

import java.io.Serializable;
import java.util.List;

import com.hotpads.datarouter.serialize.JsonAware;
import com.hotpads.datarouter.serialize.SqlAware;

public interface FieldSet extends Comparable<FieldSet>, 
		Serializable, JsonAware, SqlAware{

	List<Field<?>> getFields();
	List<String> getFieldNames();
	List<Comparable<?>> getFieldValues();
	Comparable<?> getFieldValue(String fieldName);

	String getPersistentString();  //fuse multi-column field into one string, usually with "_" characters
	String getTypedPersistentString();  //usually getDatabeanName()+"."+getPersistentString()
}
