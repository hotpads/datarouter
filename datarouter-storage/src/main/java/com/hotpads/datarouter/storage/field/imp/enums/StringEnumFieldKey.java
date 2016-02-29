package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.util.core.java.ReflectionTool;

public class StringEnumFieldKey<E extends StringEnum<E>>
extends BaseFieldKey<E>{

	private static final int DEFAULT_MAX_SIZE = MySqlColumnType.MAX_LENGTH_VARCHAR;

	private final int size;
	private final E sampleValue;

	public StringEnumFieldKey(String name, Class<E> enumClass){
		super(name);
		this.size = DEFAULT_MAX_SIZE;
		this.sampleValue = ReflectionTool.create(enumClass);
	}

	public StringEnumFieldKey(String name, String columnName, int size, Class<E> enumClass){
		super(name, columnName, true, FieldGeneratorType.NONE);
		this.size = size;
		this.sampleValue = ReflectionTool.create(enumClass);
	}

	private StringEnumFieldKey(String name, E sampleValue, String columnName, boolean nullable,
			FieldGeneratorType fieldGeneratorType, E defaultValue, int size){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
		this.size = size;
		this.sampleValue = sampleValue;
	}

	public StringEnumFieldKey<E> withSize(int size){
		return new StringEnumFieldKey<>(name, sampleValue, columnName, nullable, fieldGeneratorType, sampleValue, size);
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