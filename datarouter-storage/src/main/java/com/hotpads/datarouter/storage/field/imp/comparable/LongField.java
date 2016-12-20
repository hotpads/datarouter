package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.LongByteTool;

public class LongField extends BasePrimitiveField<Long>{

	public LongField(LongFieldKey key, Long value){
		this(null, key, value);
	}

	public LongField(String prefix, LongFieldKey key, Long value){
		super(prefix, key, value);
	}

	@Deprecated
	public LongField(String name, Long value){
		this(null, name, value);
	}

	@Deprecated
	public LongField(String prefix, String name, Long value){
		this(prefix, name, name, true, value);
	}

	@Deprecated
	public LongField(String name, boolean nullable, Long value){
		this(null, name, name, nullable, value);
	}

	@Deprecated
	public LongField(String prefix, String name, String columnName, boolean nullable, Long value){
		this(prefix, new LongFieldKey(name).withColumnName(columnName).withColumnName(columnName).withNullable(
				nullable), value);
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Long parseStringEncodedValueButDoNotSet(String str){
		if(DrStringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return Long.valueOf(str);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value == null ? null : LongByteTool.getComparableBytes(value);
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
