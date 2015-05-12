package com.hotpads.datarouter.client.imp.jdbc.field.positive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.positive.VarIntField;

public class VarIntJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Integer,VarIntField>{
	
	public VarIntJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public VarIntJdbcFieldCodec(VarIntField field){
		super(field);
	}

//	@Override
//	public Class<VarIntField> getFieldType(){
//		return VarIntField.class;
//	}



	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.INT, 11, field.getNullable(), false);
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue()==null){
				ps.setNull(parameterIndex, Types.INTEGER);
			}else{
				ps.setInt(parameterIndex, field.getValue());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Integer parseJdbcValueButDoNotSet(Object obj){
		return VarIntField.assertInRange(obj==null?null:(Integer)obj);
	}
	
	@Override
	public Integer fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			int value = rs.getInt(field.getColumnName());
			return VarIntField.assertInRange(rs.wasNull()?null:value);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
