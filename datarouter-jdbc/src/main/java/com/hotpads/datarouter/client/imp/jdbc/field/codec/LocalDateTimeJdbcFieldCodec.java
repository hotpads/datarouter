package com.hotpads.datarouter.client.imp.jdbc.field.codec;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.custom.LocalDateTimeField;
import com.hotpads.datarouter.storage.field.imp.custom.LocalDateTimeFieldKey;

public class LocalDateTimeJdbcFieldCodec extends BaseJdbcFieldCodec<LocalDateTime,LocalDateTimeField>{

	public LocalDateTimeJdbcFieldCodec(){// no-arg for reflection
		this(null);
	}

	public LocalDateTimeJdbcFieldCodec(LocalDateTimeField field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.DATETIME, field.getNumFractionalSeconds(),
				allowNullable && field.getKey().isNullable(), false);
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
				Timestamp timestamp = Timestamp.from(value.atZone(ZoneOffset.systemDefault()).toInstant());
				timestamp.setNanos(value.getNano());
				ps.setTimestamp(parameterIndex, timestamp);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public LocalDateTime fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			Timestamp timestamp = rs.getTimestamp(field.getKey().getColumnName());
			if(rs.wasNull()){
				return null;
			}
			LocalDateTime time = timestamp.toLocalDateTime();
			return time.withNano(timestamp.getNanos());
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public String getSqlEscaped(){
		return "'" + getSqlDateString(field.getValue()) + "'";
	}

	public static String getSqlDateString(LocalDateTime date){
		return date.format(LocalDateTimeField.formatter);
	}

	/*********************** tests ******************************/

	public static class Tests{
		@Test
		public void testGetSqlEscaped(){
			// mysql date format is yyyy-MM-dd HH:mm:ss http://dev.mysql.com/doc/refman/5.1/en/datetime.html
			// jdbc timestamp escape format" yyyy-MM-dd HH:mm:ss.n where n is nanoseconds (not representable with Date)
			// sql insert with a string including the nanosecond value works in mysql
			String dateString = "2002-11-05 13:14:01.100";
			String dateStringZeroFractionalSeconds = "2002-11-05 13:14:01.000";
			LocalDateTimeFieldKey localDateTimeFieldKey = new LocalDateTimeFieldKey("test");

			LocalDateTime dateTime = LocalDateTime.parse(dateString, LocalDateTimeField.formatter);
			LocalDateTime dateTimeNoNanoSeconds = LocalDateTime.parse(dateStringZeroFractionalSeconds,
					LocalDateTimeField.formatter);
			Assert.assertEquals(dateTime.getNano(), 100000000);
			Assert.assertEquals(dateTimeNoNanoSeconds.getNano(), 0);

			LocalDateTime dateTimeNow = LocalDateTime.now();
			String dateStringNow = getSqlDateString(dateTimeNow);
			LocalDateTimeField testFieldNow = new LocalDateTimeField(localDateTimeFieldKey, dateTimeNow);
			Assert.assertEquals("'" + dateStringNow + "'",
					new LocalDateTimeJdbcFieldCodec(testFieldNow).getSqlEscaped());

			String dateString1 = "2017-05-08 00:00:00.000";
			LocalDateTime localDateTime1 = LocalDateTime.parse(dateString1, LocalDateTimeField.formatter);
			Assert.assertEquals(getSqlDateString(localDateTime1), dateString1);

			LocalDateTimeField testField = new LocalDateTimeField(localDateTimeFieldKey, localDateTime1);
			Assert.assertEquals("'" + dateString1 + "'", new LocalDateTimeJdbcFieldCodec(testField).getSqlEscaped());
		}

		@Test
		public void testSetNanoSeconds(){
			int nano = 314102705;
			int milli = 314000000;
			LocalDateTime value = LocalDateTime.of(2015, 3, 21, 5, 6, 31, nano);
			Timestamp timestamp = new Timestamp(value.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
			Assert.assertNotEquals(timestamp.getNanos(), nano);
			Assert.assertEquals(timestamp.getNanos(), milli);
			timestamp.setNanos(value.getNano());
			LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.getTime()), ZoneOffset.UTC);
			Assert.assertNotEquals(time, value);
			Assert.assertEquals(time.getNano(), milli);
			Assert.assertEquals(time.withNano(nano), value);
		}
	}
}
