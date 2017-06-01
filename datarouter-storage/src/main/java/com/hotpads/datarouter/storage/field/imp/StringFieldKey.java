package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class StringFieldKey extends BaseFieldKey<String>{

	private static final int DEFAULT_MAX_SIZE = MySqlColumnType.DEFAULT_LENGTH_VARCHAR;

	//TODO expose key configuration for those defaults
	public static final MySqlCharacterSet DEFAULT_CHARACTER_SET = MySqlCharacterSet.utf8mb4;
	public static final MySqlCollation DEFAULT_COLLATION = MySqlCollation.utf8mb4_bin;

	private final int size;

	public StringFieldKey(String name){
		super(name, String.class);
		this.size = DEFAULT_MAX_SIZE;
	}

	/**
	 * @deprecated use {@link #StringFieldKey(String)} and {@link #withSize(int)}
	 */
	@Deprecated
	public StringFieldKey(String name, int size){
		super(name, String.class);
		this.size = size;
	}

	public StringFieldKey(String name, boolean nullable, int size){
		super(name, nullable, String.class, FieldGeneratorType.NONE);
		this.size = size;
	}

	public StringFieldKey(String name, String columnName, boolean nullable, int size){
		super(name, columnName, nullable, String.class, FieldGeneratorType.NONE);
		this.size = size;
	}

	private StringFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			String defaultValue, int size){
		super(name, columnName, nullable, String.class, fieldGeneratorType, defaultValue);
		this.size = size;
	}

	/*-------------------- with -------------------------*/

	public StringFieldKey withSize(int sizeOverride){
		return new StringFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, sizeOverride);
	}

	public StringFieldKey withColumnName(String columnNameOverride){
		return new StringFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue, size);
	}

	public StringFieldKey withFieldGeneratorType(FieldGeneratorType fieldGeneratorTypeOverride){
		return new StringFieldKey(name, columnName, nullable, fieldGeneratorTypeOverride, defaultValue, size);
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

	@Override
	public StringField createValueField(final String value){
		return new StringField(this, value);
	}
}