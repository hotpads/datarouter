package com.hotpads.datarouter.storage.field.imp.array;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class PrimitiveLongArrayField extends BaseField<long[]>{

	private PrimitiveLongArrayFieldKey key;

	public PrimitiveLongArrayField(PrimitiveLongArrayFieldKey key, long[] value){
		super(null, value);
		this.key = key;
	}

	@Deprecated
	public PrimitiveLongArrayField(String name, long[] value){
		this(null, name, value);
	}

	@Deprecated
	public PrimitiveLongArrayField(String prefix, String name, long[] value){
		super(prefix, value);
		this.key = new PrimitiveLongArrayFieldKey(name);
	}


	@Override
	public PrimitiveLongArrayFieldKey getKey(){
		return key;
	}

	@Override
	public String getValueString(){
		return value.toString();
	}

	@Override
	public int compareTo(Field<long[]> field){
		if(field == null){
			return 1;
		}
		return toString().compareTo(field.toString());
	}

	@Override
	public String getStringEncodedValue(){
		throw new NotImplementedException();
	}

	@Override
	public long[] parseStringEncodedValueButDoNotSet(String value){
		throw new NotImplementedException();
	}

	@Override
	public byte[] getBytes(){
		return LongByteTool.getComparableByteArray(value);
	}

	@Override
	public long[] fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return LongByteTool.fromComparableByteArray(bytes);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new NotImplementedException();
	}
}
