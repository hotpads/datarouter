package com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;

public abstract class BaseByteJdbcFieldCodec<F extends Field<Byte>> extends BasePrimitiveJdbcFieldCodec<Byte,F>{

	public BaseByteJdbcFieldCodec(F field){
		super(field);
	}

	protected abstract Integer getMaxColumnLength();
	protected abstract MySqlColumnType getMysqlColumnType();
	protected abstract Integer getJavaSqlType();

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getKey().getColumnName(), getMysqlColumnType(), getMaxColumnLength(),
				field.getKey().isNullable(), false);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue()==null){
				ps.setNull(parameterIndex, getJavaSqlType());
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
			byte value = rs.getByte(field.getKey().getColumnName());
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

}
