package com.hotpads.datarouter.storage.field;

import java.io.Serializable;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.Fielder;

public interface FieldSet<F extends FieldSet<F>> 
extends Comparable<FieldSet<F>>, 
		Fielder<F>, 
		Serializable //hibernate composite keys must implement serializable{  
{
	
	List<Field<?>> getFields();
	List<String> getFieldNames();
	List<?> getFieldValues();
	Object getFieldValue(String fieldName);

	String getPersistentString();  //fuse multi-column field into one string, usually with "_" characters
	String getTypedPersistentString();  //usually getDatabeanName()+"."+getPersistentString()
	void fromPersistentString(String s);  //currently separated by "_"... need escaping
}
