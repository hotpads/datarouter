package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;
import com.hotpads.util.core.exception.NotImplementedException;

public class UInt63ArrayField extends BaseField<List<Long>>{

	public UInt63ArrayField(String name, List<Long> value){
		super(name, value);
	}

	public UInt63ArrayField(String prefix, String name, List<Long> value){
		super(prefix, name, value);
	}
	
	@Override
	public int compareTo(Field<List<Long>> other){
		return ListTool.compare(this.value, other.getValue());
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, this.value==null?null:LongByteTool.getUInt63Bytes(this.value));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public List<Long> parseJdbcValueButDoNotSet(Object obj){
		throw new NotImplementedException("code needs testing");
//		if(obj==null){ return null; }
//		byte[] bytes = (byte[])obj;
//		return new LongArray(LongByteTool.fromUInt63Bytes(bytes));
	}
	
	@Override
	public List<Long> fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte[] bytes = rs.getBytes(this.name);
			return new LongArray(LongByteTool.fromUInt63Bytes(bytes));
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
		return LongByteTool.getUInt63Bytes(value);
	}

}
