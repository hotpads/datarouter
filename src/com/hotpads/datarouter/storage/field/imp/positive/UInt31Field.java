package com.hotpads.datarouter.storage.field.imp.positive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Random;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.number.RandomTool;

public class UInt31Field extends BasePrimitiveField<Integer>{

	public UInt31Field(String name, Integer value){
		super(name, value);
	}

	public UInt31Field(String prefix, String name, Integer value){
		super(prefix, name, value);
	}
	
	/************************ static *********************************/

	private static final Random random = new Random();

	public static int nextPositiveRandom(){
		return RandomTool.nextPositiveInt(random);
	}
	
	
	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}
	
	@Override
	public Integer parseStringEncodedValueButDoNotSet(String s){
		if(StringTool.isEmpty(s) || s.equals("null")){ return null; }
		return Integer.valueOf(s);
	}
	

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:IntegerByteTool.getUInt31Bytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}
	
	@Override
	public Integer fromBytesButDoNotSet(byte[] bytes, int offset){
		return IntegerByteTool.fromUInt31Bytes(bytes, offset);
	}
	

	/*********************** SqlEncodedField ***********************/

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.INT, 11, nullable, false);
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
	public Integer parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Integer)obj;
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
	
//	@Override
//	public void setFieldUsingReflection(FieldSet fieldSet, String fieldName, Long value){
//		try{
//			java.lang.reflect.Field fld = fieldSet.getClass().getField(fieldName);
//			fld.setAccessible(true);
//			fld.setLong(fieldSet, value);
//		}catch(Exception e){
//			throw new DataAccessException(e.getClass().getSimpleName()+" on "+fieldSet.getClass().getSimpleName()+"."+fieldName);
//		}
//	}

}
