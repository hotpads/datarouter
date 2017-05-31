package com.hotpads.datarouter.client.imp.mysql.field.codec.primitive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.mysql.field.codec.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;

public class IntegerJdbcFieldCodec extends BasePrimitiveJdbcFieldCodec<Integer,Field<Integer>>{

	public IntegerJdbcFieldCodec(){// no-arg for reflection
		this(null);
	}

	public IntegerJdbcFieldCodec(Field<Integer> field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.INT, 11,
				allowNullable && field.getKey().isNullable(), false);
	}

	@Override
	public Integer parseJdbcValueButDoNotSet(Object obj){
		return obj == null ? null : (Integer)obj;
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.INTEGER);
			}else{
				ps.setInt(parameterIndex, field.getValue());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public Integer fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			int value = rs.getInt(field.getKey().getColumnName());
			return rs.wasNull() ? null : value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
