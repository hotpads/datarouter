package com.hotpads.datarouter.client.imp.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.util.core.DrRegexTool;

public class StringJdbcFieldCodec
extends BaseJdbcFieldCodec<String,StringField>{
	
	public static final int DEFAULT_STRING_LENGTH = StringField.DEFAULT_STRING_LENGTH;
	
	public StringJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public StringJdbcFieldCodec(StringField field){
		super(field);
	}

	@Override
	public Class<StringField> getFieldType(){
		return StringField.class;
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		if(field.getSize() <= MySqlColumnType.MAX_LENGTH_VARCHAR){
			return new SqlColumn(field.getColumnName(), MySqlColumnType.VARCHAR, field.getSize(), field.getNullable(), false);
		}else if(field.getSize() <= MySqlColumnType.MAX_LENGTH_TEXT){
			return new SqlColumn(field.getColumnName(), MySqlColumnType.TEXT, null/*MySqlColumnType.MAX_LENGTH_TEXT.intValue()*/, field.getNullable(), false);
		}else if(field.getSize() <= MySqlColumnType.MAX_LENGTH_MEDIUMTEXT){
			return new SqlColumn(field.getColumnName(), MySqlColumnType.MEDIUMTEXT, null/*MySqlColumnType.MAX_LENGTH_MEDIUMTEXT.intValue()*/, 
					field.getNullable(), false);
		}else if(field.getSize() <= MySqlColumnType.MAX_LENGTH_LONGTEXT){
			return new SqlColumn(field.getColumnName(), MySqlColumnType.LONGTEXT, null, field.getNullable(), false);
		}
		throw new IllegalArgumentException("Unknown size:"+field.getSize());
	}
	
	@Override
	public String getSqlEscaped(){
		return escapeString(field.getValue());
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
			if(field.getValue()==null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, field.getValue());
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
			return rs.getString(field.getColumnName());
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
	
	public static class StringJdbcFieldCodecTests{
		@Test 
		public void testGetSqlEscaped(){
			Assert.assertEquals("'bill\\'s'",
					new StringJdbcFieldCodec(new StringField("tag","bill's", DEFAULT_STRING_LENGTH)).getSqlEscaped());
			
			//actual case encountered
			Assert.assertEquals("'Renter\\\\\\\\\\\\\\'s Assurance Program'", new StringJdbcFieldCodec(
					new StringField("tag","Renter\\\\\\'s Assurance Program", DEFAULT_STRING_LENGTH)).getSqlEscaped());


			Assert.assertEquals("'no apostrophes'", new StringJdbcFieldCodec(
					new StringField("tag","no apostrophes", DEFAULT_STRING_LENGTH)).getSqlEscaped());
			
		}
	}
	
}
