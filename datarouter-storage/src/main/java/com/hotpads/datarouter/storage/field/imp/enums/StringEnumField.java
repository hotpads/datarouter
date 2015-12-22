package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class StringEnumField<E extends StringEnum<E>> extends BaseField<E>{

	private final StringEnumFieldKey<E> key;
	private final StringField stringField;

	public StringEnumField(StringEnumFieldKey<E> key, E value){
		super(null, value);
		this.key = key;
		this.stringField = toStringField(this);
	}

	public StringEnumField(Class<E> enumClass, String name, E value, int size){
		this(enumClass, null, name, value, size);
	}

	public StringEnumField(Class<E> enumClass, String prefix, String name, E value, int size){
		this(enumClass, prefix, name, name, value, size);
	}

	public StringEnumField(Class<E> enumClass, String prefix, String name, String columnName, E value, int size){
		super(prefix, value);
		this.key = new StringEnumFieldKey<>(name, columnName, size, enumClass);
		this.stringField = toStringField(this);
	}

	@Override
	public StringEnumFieldKey<E> getKey(){
		return key;
	}

	/*********************** Comparable ********************************/

	@Override
	public int compareTo(Field<E> other){
		/* If we store the string in the database and are using Collating iterators and such, then we pretty much have
		 * to sort by the persistentString value of the enum even though the persistentInt or Ordinal value of the enum
		 * may sort differently. Perhaps an argument that PrimaryKeys should not be allowed to have alternate Fielders,
		 * else the java would sort differently depending on which Fielder was being used. */
		return DatarouterEnumTool.compareStringEnums(value, other.getValue());
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){
			return null;
		}
		return value.getPersistentString();
	}

	@Override
	public E parseStringEncodedValueButDoNotSet(String string){
		if(DrStringTool.isEmpty(string)){
			return null;
		}
		return key.getSampleValue().fromPersistentString(string);
	}

	/*********************** ByteEncodedField ***********************/

	public static final byte SEPARATOR = 0;

	@Override
	public byte[] getBytes(){
		return value == null ? null : StringByteTool.getUtf8Bytes(value.getPersistentString());
	}

	@Override
	public byte[] getBytesWithSeparator(){
		return stringField.getBytesWithSeparator();
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return stringField.numBytesWithSeparator(bytes, offset);
	}

	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		String stringValue = stringField.fromBytesButDoNotSet(bytes, offset);
		return key.getSampleValue().fromPersistentString(stringValue);
	}

	@Override
	public E fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		String stringValue = stringField.fromBytesWithSeparatorButDoNotSet(bytes, offset);
		return key.getSampleValue().fromPersistentString(stringValue);
	}

	@Override
	public String getValueString(){
		return value == null ? null : value.getPersistentString();
	}

	public E getSampleValue(){
		return key.getSampleValue();
	}

	public int getSize(){
		return key.getSize();
	}

	public static StringField toStringField(StringEnumField<?> stringEnumField){
		if(stringEnumField == null){
			return null;
		}
		String value = null;
		if(stringEnumField.getValue() != null){
			value = stringEnumField.getValue().getPersistentString();
		}
		return new StringField(stringEnumField.getPrefix(), stringEnumField.key.getName(),
				stringEnumField.key.getColumnName(), stringEnumField.key.isNullable(),
				value, stringEnumField.getSize());
	}

}
