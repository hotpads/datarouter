package com.hotpads.datarouter.storage.field.imp.custom;

import java.util.Date;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.LongByteTool;

public class LongDateField extends BasePrimitiveField<Date>{

	public LongDateField(LongDateFieldKey key, Date value){
		super(key, value);
	}

	@Deprecated
	public LongDateField(String name, Date value){
		this(new LongDateFieldKey(name), value);
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.getTime() + "";
	}

	@Override
	public Date parseStringEncodedValueButDoNotSet(String str){
		if(DrStringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		//		return DateTool.parseUserInputDate(s,null);
		return new Date(Long.valueOf(str));
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value == null ? null : LongByteTool.getUInt63Bytes(value.getTime());
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}

	@Override
	public Date fromBytesButDoNotSet(byte[] bytes, int offset){
		return new Date(LongByteTool.fromUInt63Bytes(bytes, offset));
	}
	@Override
	public String getValueString(){
		if(value == null){
			return "";
		}
		return value.toString();
	}


}
