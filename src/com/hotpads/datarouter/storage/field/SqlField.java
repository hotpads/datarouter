package com.hotpads.datarouter.storage.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface SqlField<T>{
	
	String getSqlNameValuePairEscaped();
	String getSqlEscaped();
	T parseJdbcValueButDoNotSet(Object col);
	T fromJdbcResultSetButDoNotSet(ResultSet rs);
	void setPreparedStatementValue(PreparedStatement ps, int parameterIndex);
	
	void setUsingReflection(FieldSet targetFieldSet, T value, boolean ignorePrefix);
	void fromJdbcResultSetUsingReflection(FieldSet targetFieldSet, ResultSet resultSet, boolean ignorePrefix);
	void fromHibernateResultUsingReflection(FieldSet targetFieldSet, Object col, boolean ignorePrefix);
	
}
