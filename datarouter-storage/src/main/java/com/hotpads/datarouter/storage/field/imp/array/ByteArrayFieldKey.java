package com.hotpads.datarouter.storage.field.imp.array;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class ByteArrayFieldKey extends BaseFieldKey<byte[]>{

	private final int size;

	public ByteArrayFieldKey(String name){
		super(name);
		this.size = MySqlColumnType.MAX_KEY_LENGTH;
	}

	/**
	 * @deprecated use {@link #ByteArrayFieldKey(String)} and {@link #withSize(int)}
	 */
	@Deprecated
	public ByteArrayFieldKey(String name, int size){
		super(name);
		this.size = size;
	}

	private ByteArrayFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			byte[] defaultValue, int size){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
		this.size = size;
	}

	public ByteArrayFieldKey withSize(int size){
		return new ByteArrayFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, size);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

	/**************************** get/set ****************************/

	public int getSize(){
		return size;
	}

}