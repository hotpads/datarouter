package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.ListFieldKey;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class UInt7ArrayField extends BaseListField<Byte,List<Byte>>{

	public UInt7ArrayField(ListFieldKey<Byte,List<Byte>> key, List<Byte> value){
		super(key, value);
	}

	public UInt7ArrayField(String name, List<Byte> value){
		super(name, value);
	}

	public UInt7ArrayField(String prefix, String name, List<Byte> value){
		super(prefix, name, value);
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		//TODO to CSV format?
		throw new NotImplementedException();
	}

	@Override
	public List<Byte> parseStringEncodedValueButDoNotSet(String s){
		throw new NotImplementedException();
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return this.value==null?null:DrByteTool.getUInt7Bytes(this.value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		return IntegerByteTool.fromUInt31Bytes(bytes, byteOffset);
	}

	@Override
	public List<Byte> fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = numBytesWithSeparator(bytes, byteOffset) - 4;
		return DrByteTool.getArrayList(DrByteTool.fromUInt7ByteArray(bytes, byteOffset + 4, numBytes));
	}

	@Override
	public List<Byte> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = DrArrayTool.length(bytes) - byteOffset;
		return DrByteTool.getArrayList(DrByteTool.fromUInt7ByteArray(bytes, byteOffset, numBytes));
	}

	@Override
	public int compareTo(Field<List<Byte>> other){
		return DrListTool.compare(this.value, other.getValue());
	}

}
