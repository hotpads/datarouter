package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.IntegerByteTool;

public class UInt7ArrayField extends BaseListField<Byte,List<Byte>>{

	public UInt7ArrayField(UInt7ArrayFieldKey key, List<Byte> value){
		super(key, value);
	}

	@Deprecated
	public UInt7ArrayField(String name, List<Byte> value){
		super(name, value);
	}

	/*********************** StringEncodedField ******************************/

	@Override
	public List<Byte> parseStringEncodedValueButDoNotSet(String value){
		return new Gson().fromJson(value, new TypeToken<List<Byte>>(){}.getType());
	}

	/*********************** ByteEncodedField ********************************/

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