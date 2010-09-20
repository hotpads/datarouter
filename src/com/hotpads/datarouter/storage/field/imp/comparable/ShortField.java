package com.hotpads.datarouter.storage.field.imp.comparable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.bytes.ShortByteTool;

public class ShortField extends BasePrimitiveField<Short>{

	public ShortField(String name, Short value){
		super(name, value);
	}

	public ShortField(String prefix, String name, Short value){
		super(prefix, name, value);
	}
	
	@Override
	public void fromString(String s){
		this.value = s==null?null:Short.valueOf(s);
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
			short value = rs.getShort(this.name);
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public byte[] getBytes(){
		return value==null?null:ShortByteTool.getComparableBytes(this.value);
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
