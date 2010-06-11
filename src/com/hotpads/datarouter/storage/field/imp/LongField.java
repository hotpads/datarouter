package com.hotpads.datarouter.storage.field.imp;

import java.math.BigInteger;

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
	
	
	public byte[] getComparableByteArray(){
		return LongByteTool.getComparableByteArray(value);
	}

}
