package com.hotpads.datarouter.storage.field.imp;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.LongByteTool;

public class LongField extends PrimitiveField<Long>{

	public LongField(String name, Long value){
		super(name, value);
	}

	public LongField(String prefix, String name, Long value){
		super(prefix, name, value);
	}

	@Override
	public Long parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:((BigInteger)obj).longValue();
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
			return rs.getLong(this.name);
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
		return LongByteTool.getComparableByteArray(value);
	}

}
