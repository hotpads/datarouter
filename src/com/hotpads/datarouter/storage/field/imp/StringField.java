package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class StringField extends BaseField<String>{
	

	public StringField(String name, String value){
		super(name, value);
	}

	public StringField(String prefix, String name, String value){
		super(prefix, name, value);
	}
	
	@Override
	public int compareTo(Field<String> other){
		if(other==null){ return -1; }
		return ComparableTool.nullFirstCompareTo(this.getValue(), other.getValue());
	};

	public String getSqlEscaped(){
		if(value==null){
			return "null";
		}
		String stringValue = (String)value;
		return "'" + stringValue.replaceAll("'", "''") + "'";
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public String parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(String)obj;
	}
	
	@Override
	public String fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getString(this.name);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	/******************** bytes *****************************/
	
	public static final byte SEPARATOR = 0;
	
	@Override
	public boolean isFixedLength(){
		return false;
	}
	
	@Override
	public byte[] getBytes(){
		byte[] bytes = StringByteTool.getUtf8Bytes(this.value);
		return bytes;
	}
	
	@Override
	public byte[] getBytesWithSeparator(){
		byte[] dataBytes = getBytes();
		if(ArrayTool.containsUnsorted(dataBytes, SEPARATOR)){
			throw new IllegalArgumentException("String cannot contain separator byteVal="+SEPARATOR);
		}
		if(ArrayTool.isEmpty(dataBytes)){ return new byte[]{SEPARATOR}; }
		byte[] allBytes = new byte[dataBytes.length+1];
		System.arraycopy(dataBytes, 0, allBytes, 0, dataBytes.length);
		allBytes[allBytes.length-1] = 0;//Ascii "null" will compare first in lexicographical bytes comparison
		return allBytes;
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		for(int i=offset; i < bytes.length; ++i){
			if(bytes[i]==SEPARATOR){
				return i - offset + 1;//plus 1 for the separator
			}
		}
		throw new IllegalArgumentException("separator not found");
	}
	
	@Override
	public String fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return new String(bytes, offset, length, StringByteTool.CHARSET_UTF8);
	}
	
	@Override
	public String fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		int length = numBytesWithSeparator(bytes, offset) - 1;
		return new String(bytes, offset, length, StringByteTool.CHARSET_UTF8);
	}
	
}
