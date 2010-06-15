package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class ByteArrayField extends BaseField<byte[]>{

	public ByteArrayField(String name, byte[] value){
		super(name, value);
	}

	public ByteArrayField(String prefix, String name, byte[] value){
		super(prefix, name, value);
	}
	
	@Override
	public int compareTo(Field<byte[]> other){
		return ByteTool.bitwiseCompare(this.value, other.getValue());
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, this.value==null?null:this.value);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] parseJdbcValueButDoNotSet(Object obj){
		throw new NotImplementedException("code needs testing");
//		if(obj==null){ return null; }
//		byte[] bytes = (byte[])obj;
//		return new LongArray(LongByteTool.fromUInt63Bytes(bytes));
	}
	
	@Override
	public byte[] fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getBytes(this.name);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public String getSqlEscaped(){
		throw new NotImplementedException("and probably never will be");
	};
	

	@Override
	public byte[] getBytes(){
		return this.value;
	}

}
