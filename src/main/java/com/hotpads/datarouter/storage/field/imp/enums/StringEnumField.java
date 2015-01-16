package com.hotpads.datarouter.storage.field.imp.enums;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public class StringEnumField<E extends StringEnum<E>> extends BaseField<E>{

	protected E sampleValue;
	protected int size;

	public StringEnumField(Class<E> enumClass, String name, E value, int size){
		this(enumClass, null, name, value, size);
	}

	public StringEnumField(Class<E> enumClass, String prefix, String name, E value, int size){
		super(prefix, name, value);
		this.sampleValue = ReflectionTool.create(enumClass);
		this.size = size;
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
		if(value==null){ return null; }
		return value.getPersistentString();
	}
	
	@Override
	public E parseStringEncodedValueButDoNotSet(String s){
		if(StringTool.isEmpty(s)){ return null; }
		return sampleValue.fromPersistentString(s);
	}
	

	/*********************** ByteEncodedField ***********************/
	
	public static final byte SEPARATOR = 0;

	@Override
	public boolean isFixedLength(){
		return false;
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : StringByteTool.getUtf8Bytes(value.getPersistentString());
	}

	@Override
	public byte[] getBytesWithSeparator(){
		// TODO someday don't put the separator after the last field, but that would break all currently persisted keys
		byte[] dataBytes = getBytes();
		if(ArrayTool.containsUnsorted(dataBytes, SEPARATOR)){ throw new IllegalArgumentException(
				"String cannot contain separator byteVal=" + SEPARATOR); }
		if(ArrayTool.isEmpty(dataBytes)){ return new byte[]{SEPARATOR}; }
		byte[] allBytes = new byte[dataBytes.length + 1];
		System.arraycopy(dataBytes, 0, allBytes, 0, dataBytes.length);
		allBytes[allBytes.length - 1] = SEPARATOR;// Ascii "null" will compare first in lexicographical bytes comparison
		return allBytes;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		for(int i = offset; i < bytes.length; ++i){
			if(bytes[i] == StringField.SEPARATOR){ 
				return i - offset + 1;// plus 1 for the separator
			}
		}
		int numBytes = bytes.length - offset;
		return numBytes >= 0 ? numBytes : 0; // not sure where the separator went. schema change or corruption?
		// throw new IllegalArgumentException("separator not found for bytes:"+new String(bytes));
	}

	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		if(length == 0){ return null; }// hmm - can this handle empty strings?
		//TODO use StringByteTool?
		E e = sampleValue.fromPersistentString(new String(bytes, offset, length, StringByteTool.CHARSET_UTF8));
		return e;
	}
	
	@Override
	public E fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		int lengthIncludingSeparator = numBytesWithSeparator(bytes, offset);
		if(lengthIncludingSeparator <= 0){
			throw new RuntimeException("lengthIncludingSeparator="+lengthIncludingSeparator+", but should be >= 1");
		}
		boolean lastByteIsSeparator = bytes[offset + lengthIncludingSeparator - 1] == SEPARATOR;
		int lengthWithoutSeparator = lengthIncludingSeparator;
		if(lastByteIsSeparator){
			--lengthWithoutSeparator;
		}
		if (lengthWithoutSeparator == -1){
			lengthWithoutSeparator = 0;
		}
		String stringValue = StringByteTool.fromUtf8Bytes(bytes, offset, lengthWithoutSeparator);
		E e = sampleValue.fromPersistentString(stringValue);
		return e;
	}
	

	/*********************** SqlEncodedField ***********************/

	@Override
	public SqlColumn getSqlColumnDefinition(){
		if(size <= MySqlColumnType.MAX_LENGTH_VARCHAR){
			return new SqlColumn(columnName, MySqlColumnType.VARCHAR, size, nullable, false);
		}else if(size <= MySqlColumnType.MAX_LENGTH_TEXT){
			return new SqlColumn(columnName, MySqlColumnType.TEXT, null/* MySqlColumnType.MAX_LENGTH_TEXT.intValue() */, nullable, false);
		}else if(size <= MySqlColumnType.MAX_LENGTH_MEDIUMTEXT){
			return new SqlColumn(columnName, MySqlColumnType.MEDIUMTEXT, null/* MySqlColstringFumnType.MAX_LENGTH_MEDIUMTEXT.intValue
																		 * () */, nullable, false);
		}else if(size <= MySqlColumnType.MAX_LENGTH_LONGTEXT){ return new SqlColumn(columnName, MySqlColumnType.LONGTEXT,
				null, nullable, false); }
		throw new IllegalArgumentException("Unknown size:" + size);
	}

	@Override
	public String getValueString(){
		return value == null ? null : value.getPersistentString();
	}

	@Override
	public String getSqlEscaped(){
		return value == null ? "null" : "'" + value.getPersistentString() + "'";
	}

	@Override
	public E parseJdbcValueButDoNotSet(Object obj){
		return obj == null ? null : sampleValue.fromPersistentString((String)obj);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value == null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, value.getPersistentString());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public E fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			String s = rs.getString(columnName);
			return s == null ? null : sampleValue.fromPersistentString(s);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}


}
