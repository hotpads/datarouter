package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.LongByteTool;

public class LongField extends BasePrimitiveField<Long>{

	public LongField(LongFieldKey key, Long value){
		super(key, value);
	}

	@Deprecated
	public LongField(String name, Long value){
		super(name, value);
	}

	@Deprecated
	public LongField(String prefix, String name, Long value){
		super(prefix, name, value);
	}

	@Deprecated
	public LongField(String name, boolean nullable, Long value){
		super(null, name, nullable, FieldGeneratorType.NONE, value);
	}

	@Deprecated
	public LongField(String prefix, String name, String columnName, boolean nullable, Long value){
		super(prefix, name, columnName, nullable, FieldGeneratorType.NONE, value);
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}

	@Override
	public Long parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){ return null; }
		return Long.valueOf(s);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:LongByteTool.getComparableBytes(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}

	@Override
	public Long fromBytesButDoNotSet(byte[] bytes, int offset){
		return LongByteTool.fromComparableBytes(bytes, offset);
	}
}
