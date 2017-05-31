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

public class BooleanJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Boolean,Field<Boolean>>{

	public BooleanJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public BooleanJdbcFieldCodec(Field<Boolean> field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		String defaultValue = field.getKey().getDefaultValue() == null ? null
				: field.getKey().getDefaultValue().toString();
		return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.TINYINT, 1,
				allowNullable && field.getKey().isNullable(), false, defaultValue);
	}

	@Override
	public Boolean parseJdbcValueButDoNotSet(Object obj){
		return obj == null ? null : (Boolean)obj;
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.TINYINT);
			}else{
				ps.setBoolean(parameterIndex, field.getValue());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public Boolean fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			boolean value = rs.getBoolean(field.getKey().getColumnName());
			return rs.wasNull() ? null : value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
