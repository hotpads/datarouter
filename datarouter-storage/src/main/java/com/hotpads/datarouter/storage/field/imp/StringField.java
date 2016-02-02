package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldKey;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class StringField extends BaseField<String>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	private final StringFieldKey key;

	public StringField(StringFieldKey key, String value){
		super(null, value);
		this.key = key;
	}

	@Deprecated
	public StringField(String prefix, StringFieldKey key, String value){
		super(prefix, value);
		this.key = key;
	}

	@Deprecated
	public StringField(String name, String value, int size){
		this(null, name, value, size);
	}

	@Deprecated
	public StringField(String prefix, String name, String value, int size){
		super(prefix, value);
		this.key = new StringFieldKey(name, size);
	}

	@Deprecated
	public StringField(String name, boolean nullable, String value, int size){
		super(null, value);
		this.key = new StringFieldKey(name, nullable, size);
	}

	@Deprecated
	public StringField(String prefix, String name, String columnName, boolean nullable, String value, int size){
		super(prefix, value);
		this.key = new StringFieldKey(name, columnName, nullable, size);
	}


	@Override
	public FieldKey<String> getKey(){
		return key;
	}

	@Override
	public String getValueString(){
		return value;
	}

	/************************ Comparable ****************************/

	@Override
	public int compareTo(Field<String> other){
		if(other==null){
			return -1;
		}
		return DrComparableTool.nullFirstCompareTo(this.getValue(), other.getValue());
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		return value;
	}

	@Override
	public String parseStringEncodedValueButDoNotSet(String string){
		return string;
	}


	/*********************** ByteEncodedField ***********************/

	public static final byte SEPARATOR = 0;

	@Override
	public byte[] getBytes(){
		byte[] bytes = StringByteTool.getUtf8Bytes(value);
		return bytes;
	}

	@Override
	public byte[] getBytesWithSeparator(){
		//TODO someday don't put the separator after the last field, but that would break all currently persisted keys
		byte[] dataBytes = getBytes();
		if(DrArrayTool.containsUnsorted(dataBytes, SEPARATOR)){
			throw new IllegalArgumentException("String cannot contain separator byteVal="+SEPARATOR);
		}
		if(DrArrayTool.isEmpty(dataBytes)){
			return new byte[]{SEPARATOR};
		}
		byte[] allBytes = new byte[dataBytes.length+1];
		System.arraycopy(dataBytes, 0, allBytes, 0, dataBytes.length);
		allBytes[allBytes.length-1] = SEPARATOR;//Ascii "null" will compare first in lexicographical bytes comparison
		return allBytes;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		for(int i=offset; i < bytes.length; ++i){
			if(bytes[i]==SEPARATOR){
				return i - offset + 1;//plus 1 for the separator
			}
		}
		int numBytes = bytes.length - offset;
		return numBytes >= 0 ? numBytes : 0; //not sure where the separator went.  schema change or corruption?
//		throw new IllegalArgumentException("separator not found for bytes:"+new String(bytes));
	}

	@Override
	public String fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return new String(bytes, offset, length, StringByteTool.CHARSET_UTF8);
	}

	@Override
	public String fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		int lengthIncludingSeparator = numBytesWithSeparator(bytes, offset);
		if(lengthIncludingSeparator <= 0){
			throw new RuntimeException("lengthIncludingSeparator="+lengthIncludingSeparator+", but should be >= 1");
		}
		boolean lastByteIsSeparator = bytes[offset + lengthIncludingSeparator - 1] == SEPARATOR;
		int lengthWithoutSeparator = lengthIncludingSeparator;
		if(lastByteIsSeparator){
			--lengthWithoutSeparator;
		}
		if (lengthWithoutSeparator == -1) {
			lengthWithoutSeparator = 0;
		}
		return new String(bytes, offset, lengthWithoutSeparator, StringByteTool.CHARSET_UTF8);
	}

	public int getSize(){
		return key.getSize();
	}

}