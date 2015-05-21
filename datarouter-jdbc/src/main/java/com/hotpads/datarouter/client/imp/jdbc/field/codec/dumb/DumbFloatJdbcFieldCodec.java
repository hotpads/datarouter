package com.hotpads.datarouter.client.imp.jdbc.field.codec.dumb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbFloatField;

public class DumbFloatJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Float,Field<Float>>{
	
	public DumbFloatJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public DumbFloatJdbcFieldCodec(DumbFloatField field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.FLOAT, 12, field.getNullable(), false);
	}
	
	@Override
	public Float parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Float)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue()==null){
				ps.setNull(parameterIndex, Types.FLOAT);
			}else{
				ps.setFloat(parameterIndex, field.getValue());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Float fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			float value = rs.getFloat(field.getColumnName());
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
