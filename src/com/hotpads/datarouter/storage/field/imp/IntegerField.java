package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.IntegerByteTool;

public class IntegerField extends PrimitiveField<Integer>{

	public IntegerField(String name, Integer value){
		super(name, value);
	}

	public IntegerField(String prefix, String name, Integer value){
		super(prefix, name, value);
	}

	@Override
	public Integer parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Integer)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.INTEGER);
			}else{
				ps.setInt(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Integer fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getInt(this.name);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] getBytes(){
		return IntegerByteTool.getComparableByteArray(value);
	}
	
	
}
