package com.hotpads.datarouter.storage.field.imp.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.datarouter.util.core.CollectionTool;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class DelimitedStringArrayField extends BaseListField<String, List<String>>{

	private String separator;
	
	public DelimitedStringArrayField(String name, String separator, List<String> values){
		super(name, values);
		this.separator = separator;
	}
	
	public DelimitedStringArrayField(String prefix, String name, String separator, List<String> values){
		super(prefix, name, values);
		this.separator = separator;
	}
	
	
	/*********************** StringEncodedField ***********************/
	
	@Override
	public String getStringEncodedValue(){
		return encode(value, separator);
	}
	
	@Override
	public List<String> parseStringEncodedValueButDoNotSet(String s){
		return decode(s, separator);
	}
	

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		String encodedString = encode(value, separator);
		if(encodedString==null){ return null; }
		return StringByteTool.getUtf8Bytes(encodedString);
	}

	@Override
	public List<String> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		if(bytes==null){ return null; }
		String encodedString = StringByteTool.fromUtf8Bytes(bytes);
		return decode(encodedString, separator);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new NotImplementedException();
	}
	

	/*********************** SqlEncodedField ***********************/

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.LONGBLOB, Integer.MAX_VALUE , nullable, false);
	}

	@Override
	public List<String> parseJdbcValueButDoNotSet(Object col){
		if(col==null){ return null; }
		String dbValue = (String)col;
		return decode(dbValue, separator);
	}

	@Override
	public List<String> fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			String dbValue = rs.getString(columnName);
			return decode(dbValue, separator);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setString(parameterIndex, encode(value, separator));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	
	/********************* methods ***********************/
	
	private static String encode(List<String> inputs, String separator){
		if(CollectionTool.isEmpty(inputs)){ return null; }
		for(String input : inputs){
			if(input==null){ throw new IllegalArgumentException("nulls not supported"); }
			if(input.contains(separator)){ throw new IllegalArgumentException("strings cannot contain separator"); }
		}
		return Joiner.on(separator).join(inputs);
	}
	
	private static List<String> decode(String input, String separator){
		if(input==null){ return null; }
		return ListTool.create(input.split(separator));
	}
	
	/********************* tests ************************/
	
	public static class Tests{
		@Test
		public void testRoundTrip(){
			List<String> inputs = ListTool.createArrayList("abc", "xyz", "def");
			String encoded = encode(inputs, ",");
			Assert.assertEquals("abc,xyz,def", encoded);
			List<String> decoded = decode(encoded, ",");
			Assert.assertArrayEquals(inputs.toArray(), decoded.toArray());
		}
	}
	
}
