package com.hotpads.datarouter.client.imp.mysql.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.CharSequenceSqlColumn;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.mysql.field.codec.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
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
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		boolean nullable = allowNullable && field.getKey().isNullable();
		return new CharSequenceSqlColumn(field.getKey().getColumnName(), getMySqlTypeFromSize(), getNormalizedSize(),
				nullable, false, field.getKey().getDefaultValue(), StringFieldKey.DEFAULT_CHARACTER_SET,
				StringFieldKey.DEFAULT_COLLATION);
	}

	@Override
	public String getSqlEscaped(){
		return escapeString(field.getValue());
	}

	public static String escapeString(final String string){
		if(string == null){
			return "null";
		}
		String stringValue = string;
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
			if(field.getValue() == null){
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
		return obj == null ? null : (String)obj;
	}

	@Override
	public String fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getString(field.getKey().getColumnName());
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	private int getNormalizedSize(){
		if(field.getSize() <= MySqlColumnType.DEFAULT_LENGTH_VARCHAR){
			return field.getSize();
		}
		if(field.getSize() <= MySqlColumnType.MAX_LENGTH_TEXT){
			return MySqlColumnType.MAX_LENGTH_TEXT;
		}
		if(field.getSize() <= MySqlColumnType.MAX_LENGTH_MEDIUMTEXT){
			return MySqlColumnType.MAX_LENGTH_MEDIUMTEXT;
		}
		if(field.getSize() <= MySqlColumnType.MAX_LENGTH_LONGTEXT){
			return MySqlColumnType.INT_LENGTH_LONGTEXT;
		}
		throw new IllegalArgumentException("Unknown size:" + field.getSize());
	}

	private MySqlColumnType getMySqlTypeFromSize(){
		if(field.getSize() <= MySqlColumnType.DEFAULT_LENGTH_VARCHAR){
			return MySqlColumnType.VARCHAR;
		}
		if(field.getSize() <= MySqlColumnType.MAX_LENGTH_TEXT){
			return MySqlColumnType.TEXT;
		}
		if(field.getSize() <= MySqlColumnType.MAX_LENGTH_MEDIUMTEXT){
			return MySqlColumnType.MEDIUMTEXT;
		}
		if(field.getSize() <= MySqlColumnType.MAX_LENGTH_LONGTEXT){
			return MySqlColumnType.LONGTEXT;
		}
		throw new IllegalArgumentException("Unknown size:" + field.getSize());
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