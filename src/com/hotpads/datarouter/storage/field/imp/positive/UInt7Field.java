package com.hotpads.datarouter.storage.field.imp.positive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Random;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.number.RandomTool;

public class UInt7Field extends BasePrimitiveField<Byte>{

	public UInt7Field(String name, Byte value){
		super(name, value);
	}

	public UInt7Field(String prefix, String name, Byte value){
		super(prefix, name, value);
	}
	
	/************************ static *********************************/

	private static final Random random = new Random();

	public static int nextPositiveRandom(){
		return RandomTool.nextPositiveByte(random);
	}
	
	/*********************** override *******************************/

	@Override
	public void fromString(String s){
		this.value = s==null?null:Byte.valueOf(s);
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.TINYINT);
			}else{
				ps.setByte(parameterIndex, this.value);
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