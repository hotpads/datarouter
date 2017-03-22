package com.hotpads.datarouter.client.imp.jdbc.field.codec;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.custom.LocalDateTimeField;
import com.hotpads.datarouter.storage.field.imp.custom.LocalDateTimeFieldKey;

public class LocalDateTimeJdbcFieldCodec extends BaseJdbcFieldCodec<LocalDateTime,LocalDateTimeField>{

	public static final String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

	public LocalDateTimeJdbcFieldCodec(){// no-arg for reflection
		this(null);
	}

	public LocalDateTimeJdbcFieldCodec(LocalDateTimeField field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.DATETIME, field.getNumFractionalSeconds(),
				field.getKey().isNullable(), false);
	}

	@Override
	public LocalDateTime parseJdbcValueButDoNotSet(Object obj){
		return obj == null ? null : (LocalDateTime)obj;
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.DATE);
			}else{
				// sql timestamp is MySQL's datetime
				LocalDateTime value = field.getValue();
				Timestamp timestamp = new Timestamp(value.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
				ps.setTimestamp(parameterIndex, timestamp);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public LocalDateTime fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			Timestamp timeStamp = rs.getTimestamp(field.getKey().getColumnName());
			if(rs.wasNull()){
				return null;
			}
			return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp.getTime()), ZoneOffset.UTC);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public String getSqlEscaped(){
		return "'" + getSqlDateString(field.getValue()) + "'";
	}

	public static String getSqlDateString(LocalDateTime date){
		return date.format(formatter);
	}

	/*********************** tests ******************************/

	public static class Tests{
		@Test
		public void testGetSqlEscaped() throws Exception{
			// mysql date format is yyyy-MM-dd HH:mm:ss http://dev.mysql.com/doc/refman/5.1/en/datetime.html
			// jdbc timestamp escape format" yyyy-MM-dd HH:mm:ss.n where n is nanoseconds (not representable with Date)
			// sql insert with a string including the nanosecond value works in mysql
			String dateString = "2002-11-05 13:14:01.100";
			String dateStringNoFractionalSeconds = "2002-11-05 13:14:01.000";
			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
			LocalDateTime dateTimeNoNanoSeconds = LocalDateTime.parse(dateStringNoFractionalSeconds, formatter);
			LocalDateTime dateTimeNow = LocalDateTime.now();
			String dateStringNow = getSqlDateString(dateTimeNow);
			Assert.assertEquals(dateTime.getNano(), 100000000);
			Assert.assertEquals(dateTimeNoNanoSeconds.getNano(), 0);
			LocalDateTimeField testField = new LocalDateTimeField(new LocalDateTimeFieldKey("test"), dateTime);
			Assert.assertEquals("'" + dateString + "'", new LocalDateTimeJdbcFieldCodec(testField).getSqlEscaped());
			LocalDateTimeField testFieldNow = new LocalDateTimeField(new LocalDateTimeFieldKey("test"), dateTimeNow);
			Assert.assertEquals("'" + dateStringNow + "'",
					new LocalDateTimeJdbcFieldCodec(testFieldNow).getSqlEscaped());
		}
	}
}
