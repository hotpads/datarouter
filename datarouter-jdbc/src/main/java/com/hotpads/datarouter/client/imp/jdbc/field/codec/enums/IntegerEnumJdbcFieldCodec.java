package com.hotpads.datarouter.client.imp.jdbc.field.codec.enums;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.datarouter.storage.field.imp.enums.IntegerEnumField;

public class IntegerEnumJdbcFieldCodec<E extends IntegerEnum<E>>
extends BaseJdbcFieldCodec<E,IntegerEnumField<E>>{

	public IntegerEnumJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public IntegerEnumJdbcFieldCodec(IntegerEnumField<E> field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.INT, 11, field.getKey().isNullable(),
				false);
	}

	@Override
	public String getSqlEscaped(){
		if(field.getValue()==null){
			return "null";
		}
		return field.getValue().getPersistentInteger().toString();
	}

	@Override
	public E parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:field.getSampleValue().fromPersistentInteger((Integer)obj);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue()==null){
				ps.setNull(parameterIndex, Types.INTEGER);
			}else{
				ps.setInt(parameterIndex, field.getValue().getPersistentInteger());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public E fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			int rsValue = rs.getInt(field.getKey().getColumnName());
			return rs.wasNull()?null:field.getSampleValue().fromPersistentInteger(rsValue);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

}
