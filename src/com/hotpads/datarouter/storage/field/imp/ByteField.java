package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.ByteTool;

public class ByteField extends PrimitiveField<Byte>{

	public ByteField(String name, Byte value){
		super(name, value);
	}

	public ByteField(String prefix, String name, Byte value){
		super(prefix, name, value);
	}

	@Override
	public Byte parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Byte)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.TINYINT);
			}else{
				ps.setByte(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Byte fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getByte(this.name);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public byte[] getBytes(){
		return ByteTool.getComparableByteArray(value);
	}
	

}
