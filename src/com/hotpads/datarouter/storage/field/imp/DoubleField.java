package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.PrimitiveField;

public class DoubleField extends PrimitiveField<Double>{

	public DoubleField(String name, Double value){
		super(name, value);
	}

	public DoubleField(String prefix, String name, Double value){
		super(prefix, name, value);
	}

	@Override
	public Double parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Double)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.DOUBLE);
			}else{
				ps.setDouble(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Double fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getDouble(this.name);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

}
