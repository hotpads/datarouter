package com.hotpads.datarouter.storage.field.imp.dumb;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.DoubleByteTool;
/*
 * "dumb" because doesn't necessarily sort correctly in serialized form.  should prob copy
 * whatever they do in Orderly: https://github.com/zettaset/orderly
 */
public class DumbDoubleField extends BasePrimitiveField<Double>{

	public DumbDoubleField(DumbDoubleFieldKey key, Double value){
		super(key, value);
	}

	@Deprecated
	public DumbDoubleField(String name, Double value){
		super(name, value);
	}

	@Deprecated
	public DumbDoubleField(String prefix, String name, Double value){
		super(prefix, name, value);
	}

	@Deprecated
	public DumbDoubleField(String name, boolean nullable, Double value){
		super(null, name, nullable, FieldGeneratorType.NONE, value);
	}

	@Deprecated
	public DumbDoubleField(String prefix, String name, String columnName, boolean nullable, Double value){
		super(prefix, name, columnName, nullable, FieldGeneratorType.NONE, value);
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}

	@Override
	public Double parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){ return null; }
		return Double.valueOf(s);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:DoubleByteTool.getBytes(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}

	@Override
	public Double fromBytesButDoNotSet(byte[] bytes, int offset){
		return DoubleByteTool.fromBytes(bytes, offset);
	}

}
