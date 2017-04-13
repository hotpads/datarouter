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
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;

public class DumbDoubleJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Double,Field<Double>>{

	public DumbDoubleJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public DumbDoubleJdbcFieldCodec(DumbDoubleField field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		boolean nullable = allowNullable && field.getKey().isNullable();
		String defaultValue = null;
		if(field.getKey().getDefaultValue() != null){
			defaultValue = field.getKey().getDefaultValue().toString();
		}
		return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.DOUBLE, null, nullable, false,
				defaultValue);
	}

	@Override
	public Double parseJdbcValueButDoNotSet(Object obj){
		return obj == null ? null : (Double) obj;
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue() == null){
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
			double value = rs.getDouble(field.getKey().getColumnName());
			return rs.wasNull() ? null : value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
