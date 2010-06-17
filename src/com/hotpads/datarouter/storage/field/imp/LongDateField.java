package com.hotpads.datarouter.storage.field.imp;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.LongByteTool;

public class LongDateField extends PrimitiveField<Date>{

	public LongDateField(String name, Date value){
		super(name, value);
	}

	public LongDateField(String prefix, String name, Date value){
		super(prefix, name, value);
	}

	@Override
	public Date parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:new Date(((BigInteger)obj).longValue());
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.BIGINT);
			}else{
				ps.setLong(parameterIndex, this.value.getTime());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Date fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			Long value = rs.getLong(this.name);
			return value==null?null:new Date(value);
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
		return value==null?null:LongByteTool.getComparableByteArray(value.getTime());
	}

}
