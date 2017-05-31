package com.hotpads.datarouter.client.imp.mysql.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.mysql.field.codec.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;

public class DateJdbcFieldCodec extends BaseJdbcFieldCodec<Date,DateField>{

	public DateJdbcFieldCodec(){// no-arg for reflection
		this(null);
	}

	public DateJdbcFieldCodec(DateField field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.DATETIME, field.getNumDecimalSeconds(),
				allowNullable && field.getKey().isNullable(), false);
	}

	@Override
	public Date parseJdbcValueButDoNotSet(Object obj){
		return obj == null ? null : (Date)obj;
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.DATE);
			}else{
				// sql timestamp is MySQL's datetime
				ps.setTimestamp(parameterIndex, new Timestamp(field.getValue().getTime()));
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public Date fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			Timestamp timeStamp = rs.getTimestamp(field.getKey().getColumnName());
			if(rs.wasNull()){
				return null;
			}
			Date timeStampDate = new Date(timeStamp.getTime());
			return timeStampDate;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public String getSqlEscaped(){
		return "'" + getSqlDateString(field.getValue()) + "'";
	}

	public static String getSqlDateString(Date date){
		return new Timestamp(date.getTime()).toString();
	}

	/*********************** tests ******************************/

	public static class Tests{
		@Test
		public void testGetSqlEscaped() throws Exception{
			// mysql date format is yyyy-MM-dd HH:mm:ss http://dev.mysql.com/doc/refman/5.1/en/datetime.html
			// jdbc timestamp escape format" yyyy-MM-dd HH:mm:ss.n where n is nanoseconds (not representable with Date)
			// sql insert with a string including the nanosecond value works in mysql
			String dateString = "2002-11-05 13:14:01";
			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
			DateField testField = new DateField(new DateFieldKey("test"), date);
			Assert.assertEquals("'" + dateString + ".0'", new DateJdbcFieldCodec(testField).getSqlEscaped());
		}
	}
}
