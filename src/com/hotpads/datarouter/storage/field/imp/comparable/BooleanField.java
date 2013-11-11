package com.hotpads.datarouter.storage.field.imp.comparable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.BooleanByteTool;

public class BooleanField extends BasePrimitiveField<Boolean>{

	public BooleanField(String name, Boolean value){
		super(name, value);
	}

	public BooleanField(String prefix, String name, Boolean value){
		super(prefix, name, value);
	}
	
	@Override
	public Boolean parseStringValueButDoNotSet(String s){
		if(StringTool.isEmpty(s) || s.equals("null")){ return null; }
		return Boolean.valueOf(s);
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.TINYINT, 1 , nullable, false);
	}

	@Override
	public Boolean parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Boolean)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.TINYINT);
			}else{
				ps.setBoolean(parameterIndex, value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Boolean fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			boolean value = rs.getBoolean(columnName);
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] getBytes(){
		return value==null?null:BooleanByteTool.getBytes(value);
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
