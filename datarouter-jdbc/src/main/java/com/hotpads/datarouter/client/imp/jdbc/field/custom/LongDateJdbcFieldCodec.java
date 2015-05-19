package com.hotpads.datarouter.client.imp.jdbc.field.custom;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;

public class LongDateJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Date,Field<Date>>{
	
	public LongDateJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public LongDateJdbcFieldCodec(Field<Date> field){
		super(field);
	}
	

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.BIGINT, 20 , field.getNullable(), false);
	}
	@Override
	public Date parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:new Date(((BigInteger)obj).longValue());
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue()==null){
				ps.setNull(parameterIndex, Types.BIGINT);
			}else{
				ps.setLong(parameterIndex, field.getValue().getTime());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Date fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			long value = rs.getLong(field.getColumnName());
			return rs.wasNull()?null:new Date(value);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public String getSqlEscaped(){
		if(field.getValue() == null){
			return "null";
		}
		return field.getValue().getTime()+"";
	}
}
