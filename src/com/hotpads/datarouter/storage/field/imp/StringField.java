package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.RegexTool;
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
	
	@Override
	public void fromString(String s){
		this.value = s;
	}
	
	@Override
	public String getValueString(){
		return value;
	}

	public String getSqlEscaped(){
		if(value==null){
			return "null";
		}
		String stringValue = (String)value;
		//replace \ with \\
		stringValue = RegexTool.BACKSLASH_PATTERN.matcher(stringValue)
						.replaceAll(Matcher.quoteReplacement("\\\\"));
		//replace ' with \'
		stringValue = RegexTool.APOSTROPHE_PATTERN.matcher(stringValue)
						.replaceAll(Matcher.quoteReplacement("\\'"));
		return "'" + stringValue + "'";
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
			return rs.getString(columnName);
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
		int length = numBytesWithSeparator(bytes, offset) - 1;
		return new String(bytes, offset, length, StringByteTool.CHARSET_UTF8);
	}
	
	
	/********************************* tests **********************************************/
	
	public static class StringFieldTests{
		@Test public void testGetSqlEscaped(){
			Assert.assertEquals("'bill\\'s'",
					new StringField("tag","bill's").getSqlEscaped());
			
			//actual case encountered
			Assert.assertEquals("'Renter\\\\\\\\\\\\\\'s Assurance Program'", 
					new StringField("tag","Renter\\\\\\'s Assurance Program").getSqlEscaped());


			Assert.assertEquals("'no apostrophes'",
					new StringField("tag","no apostrophes").getSqlEscaped());
			
		}
	}
}
