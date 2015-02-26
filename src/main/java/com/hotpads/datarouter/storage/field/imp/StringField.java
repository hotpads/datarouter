package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrRegexTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class StringField extends BaseField<String>{
	
	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	protected int size;

	public StringField(String name, String value, int size){
		super(name, value);
		this.size = size;
	}

	public StringField(String prefix, String name, String value, int size){
		super(prefix, name, value);
		this.size = size;
	}
	
	
	/************************ Comparable ****************************/
	
	@Override
	public int compareTo(Field<String> other){
		if(other==null){ return -1; }
		return DrComparableTool.nullFirstCompareTo(this.getValue(), other.getValue());
	}
	
	
	/*********************** StringEncodedField ***********************/
	
	@Override
	public String getStringEncodedValue(){
		return value;
	}

	@Override
	public String parseStringEncodedValueButDoNotSet(String s){
		return s;
	}
	

	/*********************** ByteEncodedField ***********************/
	
	public static final byte SEPARATOR = 0;
	
	@Override
	public boolean isFixedLength(){
		return false;
	}
	
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
		if(DrArrayTool.isEmpty(dataBytes)){ return new byte[]{SEPARATOR}; }
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
		boolean lastByteIsSeparator = bytes[offset + lengthIncludingSeparator - 1] == SEPARATOR;
		int lengthWithoutSeparator = lengthIncludingSeparator;
		if(lastByteIsSeparator){
			--lengthWithoutSeparator;
		}
		if (lengthWithoutSeparator == -1)
			lengthWithoutSeparator = 0;
		return new String(bytes, offset, lengthWithoutSeparator, StringByteTool.CHARSET_UTF8);
	}
	

	/*********************** SqlEncodedField ***********************/
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		if(size <= MySqlColumnType.MAX_LENGTH_VARCHAR){
			return new SqlColumn(columnName, MySqlColumnType.VARCHAR, size, nullable, false);
		}else if(size <= MySqlColumnType.MAX_LENGTH_TEXT){
			return new SqlColumn(columnName, MySqlColumnType.TEXT, null/*MySqlColumnType.MAX_LENGTH_TEXT.intValue()*/, nullable, false);
		}else if(size <= MySqlColumnType.MAX_LENGTH_MEDIUMTEXT){
			return new SqlColumn(columnName, MySqlColumnType.MEDIUMTEXT, null/*MySqlColumnType.MAX_LENGTH_MEDIUMTEXT.intValue()*/, 
					nullable, false);
		}else if(size <= MySqlColumnType.MAX_LENGTH_LONGTEXT){
			return new SqlColumn(columnName, MySqlColumnType.LONGTEXT, null, nullable, false);
		}
		throw new IllegalArgumentException("Unknown size:"+size);
	}
	
	@Override
	public String getValueString(){
		return value;
	}

	@Override
	public String getSqlEscaped(){
		return escapeString(value);
	}
	
	public static String escapeString(final String s){
		if(s==null){
			return "null";
		}
		String stringValue = s;
		//replace \ with \\
		stringValue = DrRegexTool.BACKSLASH_PATTERN.matcher(stringValue)
						.replaceAll(Matcher.quoteReplacement("\\\\"));
		//replace ' with \'
		stringValue = DrRegexTool.APOSTROPHE_PATTERN.matcher(stringValue)
						.replaceAll(Matcher.quoteReplacement("\\'"));
		return "'" + stringValue + "'";
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, value);
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

	public static SqlColumn getMySqlTypeFromSize(String name, int size, boolean nullable){
		if(size <= MySqlColumnType.MAX_LENGTH_VARCHAR){
			return new SqlColumn(name, MySqlColumnType.VARCHAR, size, true, false);
		}else if(size <= MySqlColumnType.MAX_LENGTH_TEXT){
			return new SqlColumn(name, MySqlColumnType.TEXT, null/*MySqlColumnType.MAX_LENGTH_TEXT.intValue()*/, true, false);
		}else if(size <= MySqlColumnType.MAX_LENGTH_MEDIUMTEXT){
			return new SqlColumn(name, MySqlColumnType.MEDIUMTEXT, null/*MySqlColumnType.MAX_LENGTH_MEDIUMTEXT.intValue()*/, 
					true, false);
		}else if(size <= MySqlColumnType.MAX_LENGTH_LONGTEXT){
			return new SqlColumn(name, MySqlColumnType.LONGTEXT, null, true, false);
		}
		throw new IllegalArgumentException("Unknown size:"+size);
	}
	
	
	/********************************* tests **********************************************/
	
	public static class StringFieldTests{
		@Test public void testGetSqlEscaped(){
			Assert.assertEquals("'bill\\'s'",
					new StringField("tag","bill's", DEFAULT_STRING_LENGTH).getSqlEscaped());
			
			//actual case encountered
			Assert.assertEquals("'Renter\\\\\\\\\\\\\\'s Assurance Program'", 
					new StringField("tag","Renter\\\\\\'s Assurance Program", DEFAULT_STRING_LENGTH).getSqlEscaped());


			Assert.assertEquals("'no apostrophes'",
					new StringField("tag","no apostrophes", DEFAULT_STRING_LENGTH).getSqlEscaped());
			
		}
	}
}