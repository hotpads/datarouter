package com.hotpads.datarouter.storage.field;

public interface Field<T>
extends Comparable<Field<T>>, ByteAwareField<T>, SqlField<T>{//TODO remove SqlFrom standard field

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
	String getValueString();
	void fromString(String s);
	void setUsingReflection(FieldSet<?> targetFieldSet, Object value);

}