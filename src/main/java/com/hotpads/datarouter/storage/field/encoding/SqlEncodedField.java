package com.hotpads.datarouter.storage.field.encoding;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;

public interface SqlEncodedField<T>{
	
	SqlColumn getSqlColumnDefinition();
	String getSqlNameValuePairEscaped();
	String getSqlEscaped();
	T parseJdbcValueButDoNotSet(Object col);
	T fromJdbcResultSetButDoNotSet(ResultSet rs);
	void setPreparedStatementValue(PreparedStatement ps, int parameterIndex);
	
	void fromJdbcResultSetUsingReflection(Object targetFieldSet, ResultSet resultSet);
	void fromHibernateResultUsingReflection(Object targetFieldSet, Object col);
	
}
