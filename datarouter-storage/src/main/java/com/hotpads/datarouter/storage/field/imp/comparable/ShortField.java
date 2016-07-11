package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.ShortByteTool;

public class ShortField extends BasePrimitiveField<Short>{

	public ShortField(ShortFieldKey key, Short value){
		super(key, value);
	}

	@Deprecated
	public ShortField(String name, Short value){
		this(new ShortFieldKey(name), value);
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
	public Short parseStringEncodedValueButDoNotSet(String str){
		if(DrStringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return Short.valueOf(str);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:ShortByteTool.getComparableBytes(this.value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 2;
	}

	@Override
	public Short fromBytesButDoNotSet(byte[] bytes, int offset){
		return ShortByteTool.fromComparableBytes(bytes, offset);
	}

}
