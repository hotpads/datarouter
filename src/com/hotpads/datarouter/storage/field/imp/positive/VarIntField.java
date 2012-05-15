package com.hotpads.datarouter.storage.field.imp.positive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Random;

import com.hotpads.datarouter.client.imp.jdbc.ddl.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.number.RandomTool;
import com.hotpads.util.core.number.VarInt;

public class VarIntField extends BasePrimitiveField<Integer>{

	public VarIntField(String name, Integer value){
		super(name, assertInRange(value));
	}

	public VarIntField(String prefix, String name, Integer value){
		super(prefix, name, assertInRange(value));
	}
	
	/************************ static *********************************/

	private static final Random random = new Random();

	public static int nextRandom(){
		return RandomTool.nextPositiveInt(random);
	}
	
	/*********************** override *******************************/

	@Override
	public void fromString(String s){
		this.value = assertInRange(s==null?null:Integer.valueOf(s));
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.INT, 11, true);
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
		return assertInRange(obj==null?null:(Integer)obj);
	}
	
	@Override
	public Integer fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			int value = rs.getInt(columnName);
			return assertInRange(rs.wasNull()?null:value);
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
		return value==null?null:new VarInt(value).getBytes();
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return new VarInt(bytes, offset).getNumBytes();
	}
	
	@Override
	public Integer fromBytesButDoNotSet(byte[] bytes, int offset){
		return new VarInt(bytes, offset).getValue();
	}
	
	/***************************** validate *****************************************/
	
	public static Integer assertInRange(Integer i){
		if(i==null){ return i; }
		if(i >= 0){ return i; }
		throw new IllegalArgumentException("VarIntField must be null or positive integer");
	}

}
