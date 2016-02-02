package com.hotpads.datarouter.storage.field.imp.dumb;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.FloatByteTool;
/*
 * "dumb" because doesn't necessarily sort correctly in serialized form.  should prob copy
 * whatever they do in Orderly: https://github.com/zettaset/orderly
 */
public class DumbFloatField extends BasePrimitiveField<Float>{

	public DumbFloatField(PrimitiveFieldKey<Float> key, Float value){
		super(key, value);
	}

	@Deprecated
	public DumbFloatField(String name, Float value){
		super(name, value);
	}

	@Deprecated
	public DumbFloatField(String prefix, String name, Float value){
		super(prefix, name, value);
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}

	@Override
	public Float parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){ return null; }
		return Float.valueOf(s);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:FloatByteTool.getBytes(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}

	@Override
	public Float fromBytesButDoNotSet(byte[] bytes, int offset){
		return FloatByteTool.fromBytes(bytes, offset);
	}

}
