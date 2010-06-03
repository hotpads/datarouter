package com.hotpads.datarouter.storage.field.imp;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.ByteTool;

public class IntegerField extends PrimitiveField<Integer>{

	public IntegerField(String name, Integer value){
		super(name, value);
	}

	public IntegerField(String prefix, String name, Integer value){
		super(prefix, name, value);
	}

	@Override
	public Integer parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Integer)obj;
	}
	
	

	public byte[] getComparableByteArray(){
		byte[] out = new byte[4];
		int shifted;
		if(value >= 0){
			shifted = value + Integer.MIN_VALUE;
		}else{
			shifted = value - Integer.MIN_VALUE;
		}
		out[3] = (byte) shifted;
		out[2] = (byte) (shifted >>>= 8);
		out[1] = (byte) (shifted >>>= 8);
		out[0] = (byte) (shifted >>>= 8);
		return out;
	}
	
	
	public static class Tests{
		@Test public void testGetOrderedBytes(){
			int a = Integer.MIN_VALUE;
			byte[] ab = new byte[]{0,0,0,0};
			Assert.assertArrayEquals(ab, new IntegerField("", a).getComparableByteArray());

			int b = Integer.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1,-1,-1};
			Assert.assertArrayEquals(bb, new IntegerField("", b).getComparableByteArray());

			int c = Integer.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,0,0,1};
			Assert.assertArrayEquals(cb, new IntegerField("", c).getComparableByteArray());

			int d = Integer.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-1,-1,-4};
			Assert.assertArrayEquals(db, new IntegerField("", d).getComparableByteArray());
		}

		@Test public void testArrays(){
			byte[] p5 = new IntegerField("", 5).getComparableByteArray();
			byte[] n3 = new IntegerField("", -3).getComparableByteArray();
			byte[] n7 = new IntegerField("", -7).getComparableByteArray();
			Assert.assertTrue(ByteTool.bitwiseCompare(p5, n3) > 0);
			Assert.assertTrue(ByteTool.bitwiseCompare(p5, n7) > 0);
		}
		
	}
}
