package com.hotpads.datarouter.storage.field;

import java.io.Serializable;
import java.util.List;

import com.hotpads.datarouter.serialize.JsonAware;
import com.hotpads.datarouter.serialize.SqlAware;

public interface FieldSet extends Comparable<FieldSet>, 
		Serializable, JsonAware, SqlAware{  //hibernate composite keys must implement serializable

	List<Field<?>> getFields();
	List<String> getFieldNames();
	List<?> getFieldValues();
	Object getFieldValue(String fieldName);

	String getPersistentString();  //fuse multi-column field into one string, usually with "_" characters
	String getTypedPersistentString();  //usually getDatabeanName()+"."+getPersistentString()
}
