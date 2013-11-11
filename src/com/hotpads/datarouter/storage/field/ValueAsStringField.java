package com.hotpads.datarouter.storage.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;

/*
 * For encoding field values as strings, like with JSON.  Awkward name.
 */
public interface ValueAsStringField<T>{
	
	T parseStringValueButDoNotSet(String value);
	
	void fromJdbcResultSetUsingReflection(FieldSet<?> targetFieldSet, ResultSet resultSet);
	void fromHibernateResultUsingReflection(FieldSet<?> targetFieldSet, Object col);
	
}
