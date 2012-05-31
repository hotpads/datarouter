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
import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public class StringEnumField<E extends StringEnum<E>> extends BaseField<E>{
	
	protected E sampleValue;

	public StringEnumField(Class<E> enumClass, String name, E value){
		this(enumClass, null, name, value);
	}

	public StringEnumField(Class<E> enumClass, String prefix, String name, E value){
		super(prefix, name, value);
		this.sampleValue = ReflectionTool.create(enumClass);
	}
	
	@Override
	public void fromString(String s){
		this.value = s==null?null:sampleValue.fromPersistentString(s);
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.VARCHAR, 250 , true);
	}
	
	@Override
	public int compareTo(Field<E> other){
		return DataRouterEnumTool.compareStringEnums(other.getValue(), value);
	}
	
	@Override
	public String getValueString(){
		return value==null?null:value.getPersistentString();
	}
	
	@Override
	public String getSqlEscaped(){
		return value==null?"null":"'" + value.getPersistentString() + "'";
	}

	@Override
	public E parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:sampleValue.fromPersistentString((String)obj);
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
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
			return s==null?null:sampleValue.fromPersistentString(s);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	
	/************************* bytes (mostly copied from StringField) **************************/
	
	public static final byte SEPARATOR = 0;
	
	@Override
	public boolean isFixedLength(){
		return false;
	}

	@Override
	public byte[] getBytes(){
		return value==null?null:StringByteTool.getUtf8Bytes(
				value.getPersistentString());
	}
	
	@Override
	public byte[] getBytesWithSeparator(){
		//TODO someday don't put the separator after the last field, but that would break all currently persisted keys
		byte[] dataBytes = getBytes();
		if(ArrayTool.containsUnsorted(dataBytes, SEPARATOR)){
			throw new IllegalArgumentException("String cannot contain separator byteVal="+SEPARATOR);
		}
		if(ArrayTool.isEmpty(dataBytes)){ return new byte[]{SEPARATOR}; }
		byte[] allBytes = new byte[dataBytes.length+1];
		System.arraycopy(dataBytes, 0, allBytes, 0, dataBytes.length);
		allBytes[allBytes.length-1] = SEPARATOR;//Ascii "null" will compare first in lexicographical bytes comparison
		return allBytes;
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		for(int i=offset; i < bytes.length; ++i){
			if(bytes[i]==StringField.SEPARATOR){
				return i - offset + 1;//plus 1 for the separator
			}
		}
		int numBytes = bytes.length - offset;
		return numBytes >= 0 ? numBytes : 0; //not sure where the separator went.  schema change or corruption?
//		throw new IllegalArgumentException("separator not found for bytes:"+new String(bytes));
	}
	
	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		if(length==0){ return null; }//hmm - can this handle empty strings?
		E e = sampleValue.fromPersistentString(
				new String(bytes, offset, length, StringByteTool.CHARSET_UTF8));
		return e;
	}
	
	
}
