package com.hotpads.datarouter.client.imp.jdbc.field.codec.positive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.positive.UInt7Field;

public class UInt7JdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Byte,UInt7Field>{
	
	public UInt7JdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public UInt7JdbcFieldCodec(UInt7Field field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.TINYINT, 3, field.getNullable(), false);
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue()==null){
				ps.setNull(parameterIndex, Types.TINYINT);
			}else{
				ps.setByte(parameterIndex, field.getValue());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Byte parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Byte)obj;
	}
	
	@Override
	public Byte fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte value = rs.getByte(field.getColumnName());
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
}
