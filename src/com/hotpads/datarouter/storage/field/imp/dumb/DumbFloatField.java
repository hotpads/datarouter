package com.hotpads.datarouter.storage.field.imp.dumb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.FloatByteTool;

public class DumbFloatField extends PrimitiveField<Float>{

	public DumbFloatField(String name, Float value){
		super(name, value);
	}

	public DumbFloatField(String prefix, String name, Float value){
		super(prefix, name, value);
	}

	@Override
	public Float parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Float)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.FLOAT);
			}else{
				ps.setFloat(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Float fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getFloat(this.name);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] getBytes(){
		return FloatByteTool.getBytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}
	
	@Override
	public Float fromBytesButDoNotSet(byte[] bytes, int offset){
		return FloatByteTool.fromBytes(bytes, offset);
	}
}
