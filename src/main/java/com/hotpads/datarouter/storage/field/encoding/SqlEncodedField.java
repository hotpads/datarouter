package com.hotpads.datarouter.storage.field.encoding;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.storage.field.FieldSet;

public interface SqlEncodedField<T>{
	
	SqlColumn getSqlColumnDefinition();
	String getSqlNameValuePairEscaped();
	String getSqlEscaped();
	T parseJdbcValueButDoNotSet(Object col);
	T fromJdbcResultSetButDoNotSet(ResultSet rs);
	void setPreparedStatementValue(PreparedStatement ps, int parameterIndex);
	
	void fromJdbcResultSetUsingReflection(FieldSet<?> targetFieldSet, ResultSet resultSet);
	void fromHibernateResultUsingReflection(FieldSet<?> targetFieldSet, Object col);
	
}