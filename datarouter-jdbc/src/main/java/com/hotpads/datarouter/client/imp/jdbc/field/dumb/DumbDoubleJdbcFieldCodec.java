package com.hotpads.datarouter.client.imp.jdbc.field.dumb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;

public class DumbDoubleJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Double,DumbDoubleField>{
	
	public DumbDoubleJdbcFieldCodec(DumbDoubleField field){
		super(field);
	}

	@Override
	public Class<DumbDoubleField> getFieldType(){
		return DumbDoubleField.class;
	}



	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.DOUBLE, 22, field.getNullable(), false);
	}
	
	@Override
	public Double parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Double)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue()==null){
				ps.setNull(parameterIndex, Types.DOUBLE);
			}else{
				ps.setDouble(parameterIndex, field.getValue());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Double fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			double value = rs.getDouble(field.getColumnName());
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
