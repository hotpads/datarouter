package com.hotpads.datarouter.storage.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface SqlField<T>{
	
	String getSqlNameValuePairEscaped();
	String getSqlEscaped();
	T parseJdbcValueButDoNotSet(Object col);
	T fromJdbcResultSetButDoNotSet(ResultSet rs);
	void setPreparedStatementValue(PreparedStatement ps, int parameterIndex);
	
	void setFieldUsingBeanUtils(FieldSet targetFieldSet, Object col);
	void fromJdbcResultSetUsingReflection(FieldSet targetFieldSet, ResultSet resultSet);
	void setFieldUsingReflection(FieldSet targetFieldSet, Object col);
	
}
