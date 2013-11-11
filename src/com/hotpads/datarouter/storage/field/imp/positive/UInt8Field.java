package com.hotpads.datarouter.storage.field.imp.positive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.StringTool;

public class UInt8Field extends BasePrimitiveField<Byte>{

	public UInt8Field(String name, Integer intValue){
		super(name, ByteTool.toUnsignedByte(intValue));
	}

	public UInt8Field(String prefix, String name, Integer intValue){
		super(prefix, name, ByteTool.toUnsignedByte(intValue));
	}
	
	/************************ static *********************************/
	
//	protected static Integer checkRange(Integer value){
//		if(value==null){ return null; }
//		if(value < 0 || value > 255){ 
//			throw new IllegalArgumentException("UInt8 must be 0-255");
//		}
//		return value;
//	}

//	private static final Random random = new Random();
//
//	public static int nextPositiveRandom(){
//		
//	}
	
	/*********************** override *******************************/

	@Override
	public Byte parseStringValueButDoNotSet(String s){
		if(StringTool.isEmpty(s) || s.equals("null")){
			return null; 
		}
		return ByteTool.toUnsignedByte(Integer.valueOf(s));
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.SMALLINT, 5, nullable, false);
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.TINYINT);
			}else{
				ps.setByte(parameterIndex, value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Byte parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Byte)obj;
	}
	
	@Override
	public Byte fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte value = rs.getByte(columnName);
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

	@Override
	public byte[] getBytes(){
		return value==null?null:new byte[]{value};
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 1;
	}
	
	@Override
	public Byte fromBytesButDoNotSet(byte[] bytes, int offset){
		return bytes[offset];
	}

}
