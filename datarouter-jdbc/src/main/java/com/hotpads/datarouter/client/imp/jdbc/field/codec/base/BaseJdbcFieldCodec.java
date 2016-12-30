package com.hotpads.datarouter.client.imp.jdbc.field.codec.base;

import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodec;
import com.hotpads.datarouter.storage.field.Field;

public abstract class BaseJdbcFieldCodec<T,F extends Field<T>> implements JdbcFieldCodec<T,F>{

	protected F field;

	public BaseJdbcFieldCodec(F field){
		this.field = field;
	}

	@Override
	public F getField(){
		return field;
	}

	@Override
	public String getSqlNameValuePairEscaped(){
		if(field.getValue() == null){
			return field.getKey().getColumnName() + " is null";
		}
		return field.getKey().getColumnName() + "=" + getSqlEscaped();
	}

	@Override
	public void fromJdbcResultSetUsingReflection(Object targetFieldSet, ResultSet resultSet){
		T value = fromJdbcResultSetButDoNotSet(resultSet);
		field.setUsingReflection(targetFieldSet, value);
	}

	@Override
	public void fromHibernateResultUsingReflection(Object targetFieldSet, Object col){
		T value = parseJdbcValueButDoNotSet(col);
		field.setUsingReflection(targetFieldSet, value);
	}
}
