package com.hotpads.datarouter.storage.field;

public interface Field<T>
extends Comparable<Field<T>>, ByteAwareField<T>, SqlField<T>{//TODO remove SqlFrom standard field

	String getPrefixedName();

	/********************************** get/set ******************************************/

	void setPrefix(String prefix);

	String getPrefix();

	String getName();
	byte[] getNameBytes();

	boolean isCollection();
	
	T getValue();
	String getValueString();

	void setName(String name);

	void setValue(T value);
	
	void fromString(String s);
	
	void setUsingReflection(FieldSet targetFieldSet, Object value, boolean ignorePrefix);

}