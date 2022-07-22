/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.model.field;

import io.datarouter.model.field.encoding.BinaryKeyField;
import io.datarouter.model.field.encoding.BinaryValueField;
import io.datarouter.model.field.encoding.StringEncodedField;

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
		BinaryKeyField<T>,
		BinaryValueField<T>{

	FieldKey<T> getKey();

	String getPrefix();
	String getPrefixedName();

	//TODO should be immutable
	Field<T> setPrefix(String prefix);

	Field<T> setValue(T value);
	T getValue();

	void setUsingReflection(Object targetFieldSet, Object value);

	int getValueHashCode();

	/**
	 * @return a human-readable string for use in toString() methods.  Should not be used for persistence.
	 */
	String getValueString();

	/**
	 * Parse the result of StringEncodedField::parseStringEncodedValueButDoNotSet and apply to the current object
	 */
	void fromString(String string);

	String getPreparedStatementValue();

	void validate();

}
