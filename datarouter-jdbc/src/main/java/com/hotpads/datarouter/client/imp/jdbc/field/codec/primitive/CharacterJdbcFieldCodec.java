package com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;

public class CharacterJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Character,Field<Character>>{
	
	public CharacterJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public CharacterJdbcFieldCodec(Field<Character> field){
		super(field);
	}
	
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.CHAR, 1, field.getNullable(), false);
	}
	
	@Override
	public Character parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Character)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue()==null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, field.getValue()==null?null:field.getValue()+"");
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Character fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			String value = rs.getString(field.getColumnName());
			return rs.wasNull()?null:value.charAt(0);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
