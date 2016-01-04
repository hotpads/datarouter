
package com.hotpads.datarouter.storage.field;

import com.hotpads.datarouter.storage.field.encoding.ByteEncodedField;
import com.hotpads.datarouter.storage.field.encoding.StringEncodedField;

/**
 * A Field consists of an immutable FieldKey and a value object. It is mainly a wrapper object to carry the key/value
 * from a Databean to the database.
 *
 * During normal operation, many Field objects will be allocated with very short lifespans. They are allocated for
 * PrimaryKey fields when calling equals() and compareTo() on PrimaryKeys or Databeans. They're also allocated every
 * time a Databean is saved.
 */
public interface Field<T>
extends Comparable<Field<T>>,
		StringEncodedField<T>,
		ByteEncodedField<T>{

	FieldKey<T> getKey();

	String getPrefix();
	String getPrefixedName();

	//TODO should be immutable
	Field<T> setPrefix(String prefix);

	Field<T> setValue(T value);
	T getValue();

	void setUsingReflection(Object targetFieldSet, Object value);

	String getValueString();
	void fromString(String string);
}