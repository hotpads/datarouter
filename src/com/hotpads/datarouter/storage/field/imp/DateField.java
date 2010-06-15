package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.LongByteTool;

public class DateField extends PrimitiveField<Date>{

	public DateField(String name, Date value){
		super(name, value);
	}

	public DateField(String prefix, String name, Date value){
		super(prefix, name, value);
	}

	@Override
	public Date parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Date)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.DATE);
			}else{
				ps.setDate(parameterIndex, new java.sql.Date(this.value.getTime()));
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Date fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getDate(this.name);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public byte[] getBytes(){
		return LongByteTool.getUInt63Bytes(this.value.getTime());
	}
}
