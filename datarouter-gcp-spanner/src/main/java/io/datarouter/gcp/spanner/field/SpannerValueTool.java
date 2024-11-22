/*
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
package io.datarouter.gcp.spanner.field;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.google.cloud.ByteArray;
import com.google.cloud.Timestamp;
import com.google.cloud.spanner.Value;

public class SpannerValueTool{

	/*-------- bool --------*/

	public static Value ofBoolean(Boolean value){
		return Value.bool(value);
	}

	/*-------- bytes --------*/

	public static Value ofBytes(byte[] value){
		return Value.bytes(toGoogleByteArray(value));
	}

	/*-------- date --------*/

	public static Value ofLocalDate(LocalDate value){
		return Value.date(toGoogleDate(value));
	}

	/*-------- float64 --------*/

	public static Value ofFloat(Float value){
		return Value.float64(toBoxedDouble(value));
	}

	public static Value ofDouble(Double value){
		return Value.float64(value);
	}

	/*-------- int64 --------*/

	public static Value ofShort(Short value){
		return Value.int64(toBoxedLong(value));
	}

	public static Value ofInteger(Integer value){
		return Value.int64(toBoxedLong(value));
	}

	public static Value ofLong(Long value){
		return Value.int64(value);
	}

	/*-------- string --------*/

	public static Value ofString(String value){
		return Value.string(value);
	}

	/*-------- timestamp --------*/

	public static Value ofDate(Date value){
		return Value.timestamp(toGoogleTimestamp(value));
	}

	public static Value ofInstant(Instant value){
		return Value.timestamp(toGoogleTimestamp(value));
	}

	public static Value ofLocalDateTime(LocalDateTime value){
		return Value.timestamp(toGoogleTimestamp(value));
	}

	public static Value ofEpochMillis(Long value){
		return Value.timestamp(toGoogleTimestampFromEpochMilli(value));
	}

	/*-------- null-safe boxed converteres --------*/

	private static Double toBoxedDouble(Float value){
		return value == null ? null : value.doubleValue();
	}

	private static Long toBoxedLong(Integer value){
		return value == null ? null : value.longValue();
	}

	private static Long toBoxedLong(Short value){
		return value == null ? null : value.longValue();
	}

	/*-------- null-safe google converteres --------*/

	private static ByteArray toGoogleByteArray(byte[] value){
		return value == null ? null : ByteArray.copyFrom(value);
	}

	private static com.google.cloud.Date toGoogleDate(LocalDate value){
		return value == null
				? null
				: com.google.cloud.Date.fromYearMonthDay(
						value.getYear(),
						value.getMonthValue(),
						value.getDayOfMonth());
	}

	private static Timestamp toGoogleTimestamp(Date value){
		return value == null ? null : Timestamp.of(value);
	}

	private static Timestamp toGoogleTimestamp(Instant value){
		return value == null ? null : Timestamp.of(java.sql.Timestamp.from(value));
	}

	private static Timestamp toGoogleTimestamp(LocalDateTime value){
		return value == null ? null : Timestamp.of(java.sql.Timestamp.valueOf(value));
	}

	public static Timestamp toGoogleTimestampFromEpochMilli(Long milliseconds){
		return milliseconds == null ? null : Timestamp.of(new java.sql.Timestamp(milliseconds));
	}
}
