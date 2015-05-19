package com.hotpads.datarouter.client.imp.jdbc.field.enums;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;

public class StringEnumJdbcFieldCodec<E extends StringEnum<E>>
extends BaseJdbcFieldCodec<E,StringEnumField<E>>{
	
	public StringEnumJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public StringEnumJdbcFieldCodec(StringEnumField<E> field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(){
		if(field.getSize() <= MySqlColumnType.MAX_LENGTH_VARCHAR){
			return new SqlColumn(field.getColumnName(), MySqlColumnType.VARCHAR, field.getSize(), field.getNullable(), false);
		}else if(field.getSize() <= MySqlColumnType.MAX_LENGTH_TEXT){
			return new SqlColumn(field.getColumnName(), MySqlColumnType.TEXT, null, field.getNullable(), false);
		}else if(field.getSize() <= MySqlColumnType.MAX_LENGTH_MEDIUMTEXT){
			return new SqlColumn(field.getColumnName(), MySqlColumnType.MEDIUMTEXT, null, field.getNullable(), false);
		}else if(field.getSize() <= MySqlColumnType.MAX_LENGTH_LONGTEXT){ return new SqlColumn(field.getColumnName(), MySqlColumnType.LONGTEXT,
				null, field.getNullable(), false); }
		throw new IllegalArgumentException("Unknown size:" + field.getSize());
	}

	@Override
	public String getSqlEscaped(){
		return field.getValue() == null ? "null" : "'" + field.getValue().getPersistentString() + "'";
	}

	@Override
	public E parseJdbcValueButDoNotSet(Object obj){
		return obj == null ? null : field.getSampleValue().fromPersistentString((String)obj);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, field.getValue().getPersistentString());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public E fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			String s = rs.getString(field.getColumnName());
			return s == null ? null : field.getSampleValue().fromPersistentString(s);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
}
