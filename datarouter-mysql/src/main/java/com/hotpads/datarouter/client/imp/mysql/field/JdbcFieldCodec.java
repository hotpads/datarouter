package com.hotpads.datarouter.client.imp.mysql.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlColumn;
import com.hotpads.datarouter.storage.field.Field;

public interface JdbcFieldCodec<T,F extends Field<T>>{

	F getField();

	SqlColumn getSqlColumnDefinition(boolean allowNullable);
	String getSqlNameValuePairEscaped();
	String getSqlEscaped();
	T parseJdbcValueButDoNotSet(Object col);
	T fromJdbcResultSetButDoNotSet(ResultSet rs);
	void setPreparedStatementValue(PreparedStatement ps, int parameterIndex);

	void fromJdbcResultSetUsingReflection(Object targetFieldSet, ResultSet resultSet);

	//TODO get me out of here
	void fromHibernateResultUsingReflection(Object targetFieldSet, Object col);
}