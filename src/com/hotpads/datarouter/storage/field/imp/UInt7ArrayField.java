package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class UInt7ArrayField extends BaseField<List<Byte>>{

	public UInt7ArrayField(String name, List<Byte> value){
		super(name, value);
	}

	public UInt7ArrayField(String prefix, String name, List<Byte> value){
		super(prefix, name, value);
	}
	
	@Override
	public int compareTo(Field<List<Byte>> other){
		return ListTool.compare(this.value, other.getValue());
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, this.value==null?null:ByteTool.getUInt7Bytes(this.value));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public List<Byte> parseJdbcValueButDoNotSet(Object obj){
		throw new NotImplementedException("code needs testing");
//		if(obj==null){ return null; }
//		byte[] bytes = (byte[])obj;
//		return new LongArray(LongByteTool.fromUInt63Bytes(bytes));
	}
	
	@Override
	public List<Byte> fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte[] bytes = rs.getBytes(this.name);
			return ByteTool.getArrayList(bytes);
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
		return this.value==null?null:ByteTool.getUInt7Bytes(this.value);
	}

}
