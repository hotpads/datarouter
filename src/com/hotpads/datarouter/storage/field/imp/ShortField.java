package com.hotpads.datarouter.storage.field.imp;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.field.PrimitiveField;

public class ShortField extends PrimitiveField<Short>{

	public ShortField(String name, Short value){
		super(name, value);
	}

	public ShortField(String prefix, String name, Short value){
		super(prefix, name, value);
	}

	@Override
	public Short parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Short)obj;
	}
	
	


	public byte[] getComparableByteArray(){
		byte[] out = new byte[2];
		short shifted;
		if(value >= 0){
			shifted = (short) (value + Short.MIN_VALUE);
		}else{
			shifted = (short) (value - Short.MIN_VALUE);
		}
		out[1] = (byte) shifted;
		out[0] = (byte) (shifted >>>= 8);
		return out;
	}
	
	
	public static class Tests{
		@Test public void testGetOrderedBytes(){
			short a = Short.MIN_VALUE;
			byte[] ab = new byte[]{0,0};
			Assert.assertArrayEquals(ab, new ShortField("", a).getComparableByteArray());

			short b = Short.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1};
			Assert.assertArrayEquals(bb, new ShortField("", b).getComparableByteArray());

			short c = Short.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,1};
			Assert.assertArrayEquals(cb, new ShortField("", c).getComparableByteArray());

			short d = Short.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-4};
			Assert.assertArrayEquals(db, new ShortField("", d).getComparableByteArray());
		}
	}

}
