package com.hotpads.datarouter.storage.field;

import com.hotpads.datarouter.storage.field.encoding.ByteEncodedField;
import com.hotpads.datarouter.storage.field.encoding.SqlEncodedField;
import com.hotpads.datarouter.storage.field.encoding.StringEncodedField;

public interface Field<T>
extends Comparable<Field<T>>, 
		StringEncodedField<T>,
		ByteEncodedField<T>, 
		SqlEncodedField<T> //TODO remove SqlFrom standard field
		{

	/******************* get/set ********************************/
	
	Field<T> setPrefix(String prefix);
	String getPrefix();

	Field<T> setName(String name);
	String getName();
	
	Field<T> setColumnName(String columnName);
	String getColumnName();

	Field<T> setValue(T value);
	T getValue();
	
	/*************** useful methods *************************/
	void cacheReflectionInfo(FieldSet<?> sampleFieldSet);
	
	boolean isCollection();
	String getPrefixedName();
	void setUsingReflection(FieldSet<?> targetFieldSet, Object value);

	@Deprecated
	String getValueString();
	void fromString(String s);
}