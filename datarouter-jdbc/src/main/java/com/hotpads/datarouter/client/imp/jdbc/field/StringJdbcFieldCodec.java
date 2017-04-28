package com.hotpads.datarouter.client.imp.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.CharSequenceSqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BaseJdbcFieldCodec;
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
		if(field.getSize() <= MySqlColumnType.DEFAULT_LENGTH_VARCHAR){
			return new CharSequenceSqlColumn(field.getKey().getColumnName(), MySqlColumnType.VARCHAR, field.getSize(),
					nullable, false, field.getKey().getDefaultValue(), StringFieldKey.DEFAULT_CHARACTER_SET,
					StringFieldKey.DEFAULT_COLLATION);
		}else if(field.getSize() <= MySqlColumnType.MAX_LENGTH_TEXT){
			return new CharSequenceSqlColumn(field.getKey().getColumnName(), MySqlColumnType.TEXT,
					MySqlColumnType.MAX_LENGTH_TEXT, nullable, false, field.getKey()
							.getDefaultValue(), StringFieldKey.DEFAULT_CHARACTER_SET, StringFieldKey.DEFAULT_COLLATION);
		}else if(field.getSize() <= MySqlColumnType.MAX_LENGTH_MEDIUMTEXT){
			return new CharSequenceSqlColumn(field.getKey().getColumnName(), MySqlColumnType.MEDIUMTEXT,
					MySqlColumnType.MAX_LENGTH_MEDIUMTEXT, nullable, false, field.getKey()
							.getDefaultValue(), StringFieldKey.DEFAULT_CHARACTER_SET, StringFieldKey.DEFAULT_COLLATION);
		}else if(field.getSize() <= MySqlColumnType.MAX_LENGTH_LONGTEXT){
			return new CharSequenceSqlColumn(field.getKey().getColumnName(), MySqlColumnType.LONGTEXT,
					MySqlColumnType.INT_LENGTH_LONGTEXT, nullable, false, field.getKey().getDefaultValue(),
					StringFieldKey.DEFAULT_CHARACTER_SET, StringFieldKey.DEFAULT_COLLATION);
		}
		throw new IllegalArgumentException("Unknown size:" + field.getSize());
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

	public static MySqlColumnType getMySqlTypeFromSize(int size){
		if(size <= MySqlColumnType.DEFAULT_LENGTH_VARCHAR){
			return MySqlColumnType.VARCHAR;
		}
		if(size <= MySqlColumnType.MAX_LENGTH_TEXT){
			return MySqlColumnType.TEXT;
		}
		if(size <= MySqlColumnType.MAX_LENGTH_MEDIUMTEXT){
			return MySqlColumnType.MEDIUMTEXT;
		}
		if(size <= MySqlColumnType.MAX_LENGTH_LONGTEXT){
			return MySqlColumnType.LONGTEXT;
		}
		throw new IllegalArgumentException("Unknown size:" + size);
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
