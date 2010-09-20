package com.hotpads.datarouter.storage.field.imp.positive;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Random;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.bytes.LongByteTool;

public class UInt63Field extends BasePrimitiveField<Long>{

	public UInt63Field(String name, Long value){
		super(name, value);
	}

	public UInt63Field(String prefix, String name, Long value){
		super(prefix, name, value);
	}
	
	/************************ static *********************************/

	private static final Random random = new Random();

	public static long nextRandom(){
		long possible;
		do{
			possible = random.nextLong();
		}while(possible < 0);
		return possible;
	}
	
	/*********************** override *******************************/

	@Override
	public void fromString(String s){
		this.value = s==null?null:Long.valueOf(s);
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.BIGINT);
			}else{
				ps.setLong(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Long parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:((BigInteger)obj).longValue();
	}
	
	@Override
	public Long fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			long value = rs.getLong(this.name);
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
		return value==null?null:LongByteTool.getUInt63Bytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}
	
	@Override
	public Long fromBytesButDoNotSet(byte[] bytes, int offset){
		return LongByteTool.fromUInt63Bytes(bytes, offset);
	}

}
