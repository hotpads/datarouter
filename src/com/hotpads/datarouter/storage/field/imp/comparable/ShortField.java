package com.hotpads.datarouter.storage.field.imp.comparable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.ShortByteTool;

public class ShortField extends PrimitiveField<Short>{

	public ShortField(String name, Short value){
		super(name, value);
	}

	public ShortField(String prefix, String name, Short value){
		super(prefix, name, value);
	}

	@Override
	public Short parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Short)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.SMALLINT);
			}else{
				ps.setShort(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Short fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getShort(this.name);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public byte[] getBytes(){
		return ShortByteTool.getComparableBytes(this.value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 2;
	}
	
	@Override
	public Short fromBytesButDoNotSet(byte[] bytes, int offset){
		return ShortByteTool.fromComparableBytes(bytes, offset);
	}

}
