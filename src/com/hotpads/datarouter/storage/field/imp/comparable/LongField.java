package com.hotpads.datarouter.storage.field.imp.comparable;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.LongByteTool;

public class LongField extends BasePrimitiveField<Long>{

	public LongField(String name, Long value){
		super(name, value);
	}

	public LongField(String prefix, String name, Long value){
		super(prefix, name, value);
	}
	
	@Override
	public void fromString(String s){
		if(StringTool.isEmpty(s) || s.equals("null")){ 
			this.value = null; 
			return; 
		}
		this.value = Long.valueOf(s);
	}

	@Override
	public Long parseJdbcValueButDoNotSet(Object obj){
		if(obj==null){ return null; }
		//currently handling jdbc and hibernate return types.  hibernate returns all sorts of different things
		if(obj instanceof BigInteger){ return ((BigInteger)obj).longValue(); }
		return (Long)obj;
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
	public Long fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			long value = rs.getLong(columnName);
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
		return value==null?null:LongByteTool.getComparableBytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}
	
	@Override
	public Long fromBytesButDoNotSet(byte[] bytes, int offset){
		return LongByteTool.fromComparableBytes(bytes, offset);
	}
}
