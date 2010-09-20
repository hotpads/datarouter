package com.hotpads.datarouter.storage.field.imp.comparable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.bytes.BooleanByteTool;

public class BooleanField extends BasePrimitiveField<Boolean>{

	public BooleanField(String name, Boolean value){
		super(name, value);
	}

	public BooleanField(String prefix, String name, Boolean value){
		super(prefix, name, value);
	}
	
	@Override
	public void fromString(String s){
		this.value = s==null?null:Boolean.valueOf(s);
	}

	@Override
	public Boolean parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Boolean)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.BIT);
			}else{
				ps.setBoolean(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Boolean fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			boolean value = rs.getBoolean(this.name);
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] getBytes(){
		return value==null?null:BooleanByteTool.getBytes(this.value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 1;
	}
	
	@Override
	public Boolean fromBytesButDoNotSet(byte[] bytes, int offset){
		return BooleanByteTool.fromBytes(bytes, offset);
	}

}
