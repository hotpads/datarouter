package com.hotpads.datarouter.storage.field;

import com.hotpads.datarouter.storage.field.encoding.ByteEncodedField;
import com.hotpads.datarouter.storage.field.encoding.SqlEncodedField;
import com.hotpads.datarouter.storage.field.encoding.StringEncodedField;

/**
 * 
 * @author mcorgan
 * 
 *         Field is a mapping from a java field to the datastore. When using an RDBMS, Field defines the column name,
 *         datatype, and metadata, but it is also used to transmit individual cells from java to the database. The
 *         getName() method refers to the java field's name. This name is used to choose the default datastore column
 *         name, but the datastore column name can be overridden with getColumnName().
 *         
 *         During normal operation, many Field objects will be allocated with very short lifespans.  They are allocated
 *         for PrimaryKey fields when calling equals() and compareTo() on PrimaryKeys or Databeans.  They're also
 *         allocated every time a Databean is saved.
 * 
 * @param <T>
 */
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
	void cacheReflectionInfo(Object sampleFieldSet);
	
	boolean isCollection();
	String getPrefixedName();
	void setUsingReflection(Object targetFieldSet, Object value);

	String getValueString();
	void fromString(String s);
}