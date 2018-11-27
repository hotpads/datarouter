/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.mysql.field.codec;

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

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.imp.custom.LocalDateTimeField;

public class LocalDateTimeMysqlFieldCodec extends BaseMysqlFieldCodec<LocalDateTime,LocalDateTimeField>{

	public LocalDateTimeMysqlFieldCodec(){// no-arg for reflection
		this(null);
	}

	public LocalDateTimeMysqlFieldCodec(LocalDateTimeField field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return new SqlColumn(field.getKey().getColumnName(), getMysqlColumnType(), field.getNumFractionalSeconds(),
				allowNullable && field.getKey().isNullable(), false);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.DATE);
			}else{
				// sql timestamp is MySQL's datetime
				LocalDateTime value = field.getValue();
				Timestamp timestamp = Timestamp.valueOf(value);
				ps.setTimestamp(parameterIndex, timestamp);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public LocalDateTime fromMysqlResultSetButDoNotSet(ResultSet rs){
		try{
			Timestamp timestamp = rs.getTimestamp(field.getKey().getColumnName());
			if(rs.wasNull()){
				return null;
			}
			return timestamp.toLocalDateTime();
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		return MysqlColumnType.DATETIME;
	}

	public static String getSqlDateString(LocalDateTime date){
		return date.format(LocalDateTimeField.formatter);
	}

	public static class LocalDateTimeMysqlFieldCodecTests{

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
