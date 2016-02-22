package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class StringFieldKey extends BaseFieldKey<String>{

	private static final int DEFAULT_MAX_SIZE = MySqlColumnType.MAX_LENGTH_VARCHAR;

	private final int size;

	public StringFieldKey(String name){
		super(name);
		this.size = DEFAULT_MAX_SIZE;
	}

	/**
	 * @deprecated use {@link #StringFieldKey(String)} and {@link #withSize(int)}
	 */
	@Deprecated
	public StringFieldKey(String name, int size){
		super(name);
		this.size = size;
	}

	public StringFieldKey(String name, boolean nullable, int size){
		super(name, nullable, FieldGeneratorType.NONE);
		this.size = size;
	}

	public StringFieldKey(String name, String columnName, boolean nullable, int size){
		super(name, columnName, nullable, FieldGeneratorType.NONE);
		this.size = size;
	}

	private StringFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			String defaultValue, int size){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
		this.size = size;
	}

	public StringFieldKey withSize(int size){
		return new StringFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, size);
	}

	/*********************** ByteEncodedField ***********************/

	public static final byte SEPARATOR = 0;

	@Override
	public boolean isFixedLength(){
		return false;
	}

	/**************************** get/set ****************************/

	public int getSize(){
		return size;
	}

}