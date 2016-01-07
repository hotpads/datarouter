package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.IntegerByteTool;

public class IntegerEnumField<E extends IntegerEnum<E>>
extends BaseField<E>{

	private final IntegerEnumFieldKey<E> key;

	public IntegerEnumField(IntegerEnumFieldKey<E> key, E value){
		super(null, value);
		this.key = key;
	}

	public IntegerEnumField(Class<E> enumClass, String name, E value){
		this(enumClass, null, name, value);
	}

	public IntegerEnumField(Class<E> enumClass, String prefix, String name, E value){
		super(prefix, value);
		this.key = new IntegerEnumFieldKey<>(name, enumClass);
	}

	@Override
	public IntegerEnumFieldKey<E> getKey(){
		return key;
	}


	public E getSampleValue(){
		return key.getSampleValue();
	}

	/*********************** Comparable ********************************/

	@Override
	public int compareTo(Field<E> other){
		return DatarouterEnumTool.compareIntegerEnums(value, other.getValue());
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){
			return null;
		}
		return value.getPersistentInteger().toString();
	}

	@Override
	public E parseStringEncodedValueButDoNotSet(String str){
		if(DrStringTool.isEmpty(str)){
			return null;
		}
		return key.getSampleValue().fromPersistentInteger(Integer.valueOf(str));
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:IntegerByteTool.getComparableBytes(
				value.getPersistentInteger());
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}

	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		return key.getSampleValue().fromPersistentInteger(
				IntegerByteTool.fromComparableBytes(bytes, offset));
	}



	@Override
	public String getValueString(){
		if(value==null){
			return "";
		}//hmm - should this just return null?
		return String.valueOf(value.getPersistentInteger());
	}


}
