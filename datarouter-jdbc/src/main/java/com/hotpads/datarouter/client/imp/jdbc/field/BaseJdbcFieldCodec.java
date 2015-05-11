package com.hotpads.datarouter.client.imp.jdbc.field;

import java.sql.ResultSet;

import com.hotpads.datarouter.storage.field.Field;

public abstract class BaseJdbcFieldCodec<T,F extends Field<T>>
implements JdbcFieldCodec<T,F>{

	protected F field;

	
//	public BaseJdbcFieldCodec(F field){
//		this.field = field;
//	}
	
	@Override
	public F getField(){
		return field;
	}
	
	public void setField(F field){
		this.field = field;
	}
	

	@Override
	public String getSqlNameValuePairEscaped(){
		if(field.getValue() == null){
			return field.getColumnName() + " is null";
		}
		return field.getColumnName() + "=" + getSqlEscaped();
	}
	
	@Override
	public void fromJdbcResultSetUsingReflection(Object targetFieldSet, ResultSet resultSet){
		T v = fromJdbcResultSetButDoNotSet(resultSet);
		field.setUsingReflection(targetFieldSet, v);
	}
	
}
