package com.hotpads.datarouter.storage.field.imp.array;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Hex;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.util.core.bytes.IntegerByteTool;

public class ByteArrayField extends BaseField<byte[]>{

	private ByteArrayFieldKey key;

	public ByteArrayField(String name, byte[] value, int size){
		this(null, name, value, size);
	}

	public ByteArrayField(String prefix, String name, byte[] value, int size){
		super(prefix, value);
		this.key = new ByteArrayFieldKey(name, size);
	}

	@Override
	public ByteArrayFieldKey getKey(){
		return key;
	}

	public int getSize(){
		return key.getSize();
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return DatatypeConverter.printBase64Binary(value);
	}

	@Override
	public byte[] parseStringEncodedValueButDoNotSet(String stringValue){
		return DatatypeConverter.parseBase64Binary(stringValue);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:DrByteTool.flipToAndFromComparableByteArray(this.value);
	}

	@Override
	public byte[] getBytesWithSeparator(){
		if(this.value==null){
			return null;
		}
		//prepend the length as a positive integer (not bitwise comparable =( )
		//TODO replace with varint
		//TODO write directly to the allBytes array
		byte[] dataBytes = DrByteTool.flipToAndFromComparableByteArray(value);
		byte[] allBytes = new byte[4+DrArrayTool.length(dataBytes)];
		System.arraycopy(IntegerByteTool.getUInt31Bytes(0), 0, allBytes, 4, 4);
		System.arraycopy(dataBytes, 0, allBytes, 4, DrArrayTool.length(dataBytes));
		return allBytes;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return IntegerByteTool.fromUInt31Bytes(bytes, offset);//should we be adding 4 here?
	}

	@Override
	public byte[] fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		int numBytes = numBytesWithSeparator(bytes, offset) - 4;
		return DrByteTool.flipToAndFromComparableByteArray(bytes, offset + 4, numBytes);
	}

	@Override
	public byte[] fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		int length = bytes.length - byteOffset;
		return DrByteTool.flipToAndFromComparableByteArray(bytes, byteOffset, length);
	}

	@Override
	public String getValueString(){
		if(value == null){
			return null;
		}
		return Hex.encodeHexString(value);
	}

	@Override
	public int compareTo(Field<byte[]> other){
		return DrByteTool.bitwiseCompare(this.value, other.getValue());
	}

}
