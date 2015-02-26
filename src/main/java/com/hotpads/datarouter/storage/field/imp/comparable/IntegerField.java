package com.hotpads.datarouter.storage.field.imp.comparable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.IntegerByteTool;

public class IntegerField extends BasePrimitiveField<Integer>{

	public IntegerField(String name, Integer value){
		super(name, value);
	}

	public IntegerField(String prefix, String name, Integer value){
		super(prefix, name, value);
	}
	
	
	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}
	
	@Override
	public Integer parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){ return null; }
		return Integer.valueOf(s);
	}
	

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:IntegerByteTool.getComparableBytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}
	
	@Override
	public Integer fromBytesButDoNotSet(byte[] bytes, int offset){
		return IntegerByteTool.fromComparableBytes(bytes, offset);
	}
	

	/*********************** SqlEncodedField ***********************/

	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.INT, 11, nullable, false);
	}

	@Override
	public Integer parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Integer)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.INTEGER);
			}else{
				ps.setInt(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Integer fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			int value = rs.getInt(columnName);
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}	
	
}
