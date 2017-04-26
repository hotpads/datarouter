package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.IntegerEnum;

public class IntegerEnumField<E extends IntegerEnum<E>>
extends BaseField<E>{

	private final IntegerEnumFieldKey<E> key;

	public IntegerEnumField(IntegerEnumFieldKey<E> key, E value){
		super(null, value);
		this.key = key;
	}

	@Deprecated
	public IntegerEnumField(Class<E> enumClass, String name, E value){
		this(enumClass, null, name, value);
	}

	@Deprecated
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
		if(value == null){
			return null;
		}
		return value.getPersistentInteger().toString();
	}

	@Override
	public E parseStringEncodedValueButDoNotSet(String str){
		if(str == null){
			return null;
		}
		return IntegerEnum.fromPersistentIntegerSafe(getSampleValue(), Integer.valueOf(str));
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value == null ? null : IntegerByteTool.getComparableBytes(value.getPersistentInteger());
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}

	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		return IntegerEnum.fromPersistentIntegerSafe(getSampleValue(), IntegerByteTool.fromComparableBytes(bytes,
				offset));
	}

	@Override
	public String getValueString(){
		if(value == null){
			return "";
		}//hmm - should this just return null?
		return String.valueOf(value.getPersistentInteger());
	}


}
