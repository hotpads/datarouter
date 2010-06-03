package com.hotpads.datarouter.storage.field.imp;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.field.PrimitiveField;

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
		byte[] out = new byte[8];
		long shifted;
		if(value >= 0){
			shifted = value + Long.MIN_VALUE;
		}else{
			shifted = value - Long.MIN_VALUE;
		}
		out[7] = (byte) shifted;
		out[6] = (byte) (shifted >>>= 8);
		out[5] = (byte) (shifted >>>= 8);
		out[4] = (byte) (shifted >>>= 8);
		out[3] = (byte) (shifted >>>= 8);
		out[2] = (byte) (shifted >>>= 8);
		out[1] = (byte) (shifted >>>= 8);
		out[0] = (byte) (shifted >>>= 8);
		return out;
	}
	
	
	public static class Tests{
		@Test public void testGetOrderedBytes(){
			long a = Long.MIN_VALUE;
			byte[] ab = new byte[]{0,0,0,0,0,0,0,0};
			Assert.assertArrayEquals(ab, new LongField("", a).getComparableByteArray());

			long b = Long.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1,-1,-1,-1,-1,-1,-1};
			Assert.assertArrayEquals(bb, new LongField("", b).getComparableByteArray());

			long c = Long.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,0,0,0,0,0,0,1};
			Assert.assertArrayEquals(cb, new LongField("", c).getComparableByteArray());

			long d = Long.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-1,-1,-1,-1,-1,-1,-4};
			Assert.assertArrayEquals(db, new LongField("", d).getComparableByteArray());
		}
	}

}
