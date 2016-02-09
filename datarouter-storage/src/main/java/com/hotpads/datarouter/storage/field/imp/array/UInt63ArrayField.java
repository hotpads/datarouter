package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;
import com.hotpads.util.core.exception.NotImplementedException;

public class UInt63ArrayField extends BaseListField<Long,List<Long>>{

	public UInt63ArrayField(UInt63ArrayFieldKey key, List<Long> value){
		super(key, value);
	}

	@Deprecated
	public UInt63ArrayField(String name, List<Long> value){
		super(name, value);
	}

	@Deprecated
	public UInt63ArrayField(String prefix, String name, List<Long> value){
		super(prefix, name, value);
	}


	/*********************** Comparable ********************************/

	//TODO should we even bother?
	@Override
	public int compareTo(Field<List<Long>> other){
		return DrListTool.compare(this.value, other.getValue());
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		//TODO to CSV format?
		throw new NotImplementedException();
	}

	@Override
	public List<Long> parseStringEncodedValueButDoNotSet(String s){
		throw new NotImplementedException();
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:LongByteTool.getUInt63ByteArray(value);
	}

	@Override
	public List<Long> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = DrArrayTool.length(bytes) - byteOffset;
		return new LongArray(LongByteTool.fromUInt63ByteArray(bytes, byteOffset, numBytes));
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		return bytes==null?0:IntegerByteTool.fromUInt31Bytes(bytes, byteOffset) + 4;
	}

	@Override
	public byte[] getBytesWithSeparator(){
		if(value==null){ return IntegerByteTool.getUInt31Bytes(0); }
		//prepend the length (in bytes) as a positive integer (not bitwise comparable =( )
		//TODO replace with varint
		byte[] dataBytes = LongByteTool.getUInt63ByteArray(value);
		byte[] allBytes = new byte[4+dataBytes.length];
		System.arraycopy(IntegerByteTool.getUInt31Bytes(dataBytes.length), 0, allBytes, 0, 4);
		System.arraycopy(dataBytes, 0, allBytes, 4, dataBytes.length);
		return allBytes;
	}

	@Override
	public List<Long> fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = numBytesWithSeparator(bytes, byteOffset) - 4;
		return new LongArray(LongByteTool.fromUInt63ByteArray(bytes, byteOffset + 4, numBytes));
	}


	/*********************** tests ***************************/

	public static class UInt63ArrayFieldTests{
		@Test public void testByteAware(){
			LongArray a1 = new LongArray();
			a1.add(Long.MAX_VALUE);
			a1.add(Integer.MAX_VALUE);
			a1.add(Short.MAX_VALUE);
			a1.add(Byte.MAX_VALUE);
			a1.add(5);
			a1.add(0);
			UInt63ArrayField field = new UInt63ArrayField("", a1);
			byte[] bytesNoPrefix = field.getBytes();
			Assert.assertEquals(a1.size()*8, DrArrayTool.length(bytesNoPrefix));
			List<Long> a2 = new UInt63ArrayField("", null).fromBytesButDoNotSet(bytesNoPrefix, 0);
			Assert.assertTrue(DrCollectionTool.equalsAllElementsInIteratorOrder(a1, a2));

			byte[] bytesWithPrefix = field.getBytesWithSeparator();
			Assert.assertEquals(a1.size()*8, bytesWithPrefix[3]);
			Assert.assertEquals(a1.size()*8 + 4, field.numBytesWithSeparator(bytesWithPrefix, 0));

			List<Long> a3 = new UInt63ArrayField("", null).fromBytesWithSeparatorButDoNotSet(bytesWithPrefix, 0);
			Assert.assertTrue(DrCollectionTool.equalsAllElementsInIteratorOrder(a1, a3));

		}
	}
}
