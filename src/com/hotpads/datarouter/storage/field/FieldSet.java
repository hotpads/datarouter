package com.hotpads.datarouter.storage.field;

import java.io.Serializable;
import java.util.List;

import com.hotpads.datarouter.serialize.ByteAware;
import com.hotpads.datarouter.serialize.JsonAware;
import com.hotpads.datarouter.serialize.SqlAware;

public interface FieldSet extends Comparable<FieldSet>, 
		Serializable, JsonAware, SqlAware, ByteAware{  //hibernate composite keys must implement serializable

	List<Field<?>> getFields();
	List<String> getFieldNames();
	List<?> getFieldValues();
	Object getFieldValue(String fieldName);

	List<Field<?>> getKeyFields();

	String getPersistentString();  //fuse multi-column field into one string, usually with "_" characters
	String getTypedPersistentString();  //usually getDatabeanName()+"."+getPersistentString()
	void fromPersistentString(String s);  //currently separated by "_"... need escaping
}
