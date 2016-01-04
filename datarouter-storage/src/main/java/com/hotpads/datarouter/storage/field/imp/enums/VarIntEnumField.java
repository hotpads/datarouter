package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.number.VarInt;

public class VarIntEnumField<E extends IntegerEnum<E>> extends BaseField<E>{

	private VarIntEnumFieldKey<E> key;
	private E sampleValue;
	private Class<E> enumClass;

	public VarIntEnumField(Class<E> enumClass, String name, E value){
		this(enumClass, null, name, value);
	}

	public VarIntEnumField(Class<E> enumClass, String prefix, String name, E value){
		super(prefix, value);
		this.key = new VarIntEnumFieldKey<>(name);
		this.sampleValue = ReflectionTool.create(enumClass);
		this.enumClass = enumClass;
	}

	@Override
	public VarIntEnumFieldKey<E> getKey(){
		return key;
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
	public E parseStringEncodedValueButDoNotSet(String string){
		if(DrStringTool.isEmpty(string)){
			return null;
		}
		return sampleValue.fromPersistentInteger(Integer.valueOf(string));
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:new VarInt(value.getPersistentInteger()).getBytes();
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return new VarInt(bytes, offset).getNumBytes();
	}

	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		Integer intValue = new VarInt(bytes, offset).getValue();
		return sampleValue.fromPersistentInteger(intValue);
	}

	@Override
	public String getValueString(){
		if(value==null){
			return "";//hmm - should this just return null?
		}
		return String.valueOf(value.getPersistentInteger());
	}

	public E getSampleValue(){
		return sampleValue;
	}

	public static <E extends IntegerEnum<E>> IntegerEnumField<E> toIntegerEnumField(VarIntEnumField<E> field){
		return new IntegerEnumField<>(field.enumClass, field.getPrefix(), field.key.getName(), field.getValue());
	}
}
