package com.hotpads.datarouter.storage.field.encoding;


/*
 * For encoding field values as strings, like with JSON.  Awkward name.
 */
public interface StringEncodedField<T>{
	
	String getStringEncodedValue();
	T parseStringEncodedValueButDoNotSet(String value);
	
}
