package com.hotpads.datarouter.storage.field.imp;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.ByteTool;

public class ByteField extends PrimitiveField<Byte>{

	public ByteField(String name, Byte value){
		super(name, value);
	}

	public ByteField(String prefix, String name, Byte value){
		super(prefix, name, value);
	}

	@Override
	public Byte parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Byte)obj;
	}
	
	
	public byte[] getComparableByteArray(){
		if(value >= 0){
			return new byte[]{(byte) (value + Byte.MIN_VALUE) };
		}else{
			return new byte[]{(byte) (value - Byte.MIN_VALUE) };
		}
	}
	
	
	public static class Tests{
		@Test public void testGetOrderedBytes(){
			byte min = Byte.MIN_VALUE;
			Assert.assertEquals(-128, min);
			byte max = Byte.MAX_VALUE;
			Assert.assertEquals(127, max);
			Assert.assertTrue(min < max);
			
			byte[] minArray = new ByteField("",min).getComparableByteArray();
			byte[] maxArray = new ByteField("",max).getComparableByteArray();
			Assert.assertTrue(ByteTool.bitwiseCompare(maxArray, minArray) > 0);
			
			System.out.println(ByteTool.getBinaryStringBigEndian(min)+" "
					+ByteTool.getBinaryStringBigEndian(max)+" "
					+ByteTool.getBinaryStringBigEndian(minArray)+" "
					+ByteTool.getBinaryStringBigEndian(maxArray));

			byte negative = -3;
			byte positive = 5;
			Assert.assertTrue(negative < positive);

			byte[] negativeArray = new ByteField("",negative).getComparableByteArray();
			byte[] positiveArray = new ByteField("",positive).getComparableByteArray();
			Assert.assertTrue(ByteTool.bitwiseCompare(positiveArray, negativeArray) > 0);
			
			System.out.println(ByteTool.getBinaryStringBigEndian(negative)+" "
					+ByteTool.getBinaryStringBigEndian(positive)+" "
					+ByteTool.getBinaryStringBigEndian(negativeArray)+" "
					+ByteTool.getBinaryStringBigEndian(positiveArray));
		}
	}

}
