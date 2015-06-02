package com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;

public class LongJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Long,Field<Long>>{
	
	public LongJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public LongJdbcFieldCodec(Field<Long> field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.BIGINT, 20, field.getKey().isNullable(),
				false);
	}
	
	@Override
	public Long parseJdbcValueButDoNotSet(Object obj){
		if(obj==null){ 
			return null;
		}
		//currently handling jdbc and hibernate return types.  hibernate returns all sorts of different things
		if(obj instanceof BigInteger){
			return ((BigInteger)obj).longValue();
		}
		return (Long)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue()==null){
				ps.setNull(parameterIndex, Types.BIGINT);
			}else{
				ps.setLong(parameterIndex, field.getValue());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Long fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			long value = rs.getLong(field.getKey().getColumnName());
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

}
