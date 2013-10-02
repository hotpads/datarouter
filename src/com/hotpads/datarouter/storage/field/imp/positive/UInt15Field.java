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
import com.hotpads.util.core.bytes.ShortByteTool;
import com.hotpads.util.core.number.RandomTool;

public class UInt15Field extends BasePrimitiveField<Short>{

	public UInt15Field(String name, Short value){
		super(name, value);
	}

	public UInt15Field(String prefix, String name, Short value){
		super(prefix, name, value);
	}
	
	/************************ static *********************************/

	private static final Random random = new Random();

	public static int nextPositiveRandom(){
		return RandomTool.nextPositiveShort(random);
	}
	
	/*********************** override *******************************/

	@Override
	public void fromString(String s){
		this.value = s==null?null:Short.valueOf(s);
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.SMALLINT, 5, nullable, false);
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.SMALLINT);
			}else{
				ps.setShort(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Short parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Short)obj;
	}
	
	@Override
	public Short fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			short value = rs.getShort(columnName);
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
		return value==null?null:ShortByteTool.getUInt15Bytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 2;
	}
	
	@Override
	public Short fromBytesButDoNotSet(byte[] bytes, int offset){
		return ShortByteTool.fromUInt15Bytes(bytes, offset);
	}

}
