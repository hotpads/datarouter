package com.hotpads.datarouter.storage.field;

import java.io.Serializable;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.Fielder;

public interface FieldSet<F extends FieldSet<F>>
extends Comparable<FieldSet<F>>,
		Fielder<F>,
		Serializable{ //hibernate composite keys must implement Serializable

	List<Field<?>> getFields();
	List<String> getFieldNames();
	List<?> getFieldValues();
	Object getFieldValue(String fieldName);

	@Deprecated //use PercentCodec
	String getPersistentString();  //fuse multi-column field into one string, usually with "_" characters
	@Deprecated
	String getTypedPersistentString();  //usually getDatabeanName()+"."+getPersistentString()
	@Deprecated
	void fromPersistentString(String s);  //currently separated by "_"... need escaping

}
