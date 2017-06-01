package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.util.core.enums.StringEnum;
import com.hotpads.util.core.java.ReflectionTool;

public class StringEnumFieldKey<E extends StringEnum<E>>
extends BaseFieldKey<E>{

	private static final int DEFAULT_MAX_SIZE = MySqlColumnType.DEFAULT_LENGTH_VARCHAR;

	private final int size;
	private final E sampleValue;

	public StringEnumFieldKey(String name, Class<E> enumClass){
		super(name, enumClass);
		this.size = DEFAULT_MAX_SIZE;
		this.sampleValue = ReflectionTool.create(enumClass);
	}

	public StringEnumFieldKey(String name, String columnName, int size, Class<E> enumClass){
		super(name, columnName, true, enumClass, FieldGeneratorType.NONE);
		this.size = size;
		this.sampleValue = ReflectionTool.create(enumClass);
	}

	private StringEnumFieldKey(String name, E sampleValue, String columnName, boolean nullable, Class<E> enumClass,
			FieldGeneratorType fieldGeneratorType, E defaultValue, int size){
		super(name, columnName, nullable, enumClass, fieldGeneratorType, defaultValue);
		this.size = size;
		this.sampleValue = sampleValue;
	}

	@SuppressWarnings("unchecked")
	public StringEnumFieldKey<E> withSize(int sizeOverride){
		return new StringEnumFieldKey<>(name, sampleValue, columnName, nullable, (Class<E>)getValueType(),
				fieldGeneratorType, sampleValue, sizeOverride);
	}

	@SuppressWarnings("unchecked")
	public StringEnumFieldKey<E> withColumnName(String columnNameOverride){
		return new StringEnumFieldKey<>(name, sampleValue, columnNameOverride, nullable, (Class<E>)getValueType(),
				fieldGeneratorType, sampleValue, size);
	}

	@Override
	public StringEnumField<E> createValueField(final E value){
		return new StringEnumField<>(this, value);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

	public int getSize(){
		return size;
	}

	public E getSampleValue(){
		return sampleValue;
	}

}