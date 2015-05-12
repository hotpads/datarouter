package com.hotpads.datarouter.client.imp.jdbc.field.primitive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.comparable.ShortField;

public class ShortJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Short,ShortField>{
	
	public ShortJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public ShortJdbcFieldCodec(ShortField field){
		super(field);
	}

	@Override
	public Class<ShortField> getFieldType(){
		return ShortField.class;
	}



	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.SMALLINT, 5, field.getNullable(), false);
	}
	
	@Override
	public Short parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Short)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue()==null){
				ps.setNull(parameterIndex, Types.SMALLINT);
			}else{
				ps.setShort(parameterIndex, field.getValue());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Short fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			short value = rs.getShort(field.getColumnName());
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
