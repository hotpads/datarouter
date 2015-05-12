package com.hotpads.datarouter.client.imp.jdbc.field.primitive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;

public class SignedByteJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Byte,Field<Byte>>{
	
	public SignedByteJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public SignedByteJdbcFieldCodec(Field<Byte> field){
		super(field);
	}

//	@Override
//	public Class<SignedByteField> getFieldType(){
//		return SignedByteField.class;
//	}

	

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.TINYINT, 1 , field.getNullable(), false);
	}
	
	@Override
	public Byte parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Byte)obj;
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
	public Byte fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte value = rs.getByte(field.getColumnName());
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
