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
package io.datarouter.types;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import de.huxhorn.sulky.ulid.ULID;

public record Ulid(
		String value)
implements Comparable<Ulid>{

	public static final int LENGTH = 26;

	private static final ULID ULID_INSTANCE = new ULID();

	public Ulid{
		ULID.parseULID(value);
	}

	public Ulid(){
		this(newValue());
	}

	public static String newValue(){
		return ULID_INSTANCE.nextULID();
	}

	public long getTimestampMs(){
		return ULID.parseULID(value).timestamp();
	}

	public Instant getInstant(){
		return Instant.ofEpochMilli(getTimestampMs());
	}

	/**
	 * Creates a Ulid with a given timestamp and all 0's in the randomness suffix, making it
	 * the first possible Ulid for a given timestamp when lexicographically sorted.
	 * This is primarily used to create ranges of Ulid's spanning timestamps.
	 * @param timestamp the desired timestamp to be represented in the prefix of the Ulid
	 * @return the lexicographically-first possible Ulid for a given timestamp
	 */
	public static Ulid createFirstUlidFromTimestamp(long timestamp){
		timestamp = timestamp & UlidTool.TIMESTAMP_MASK;

		char[] buffer = new char[26];

		UlidTool.internalWriteCrockford(buffer, timestamp, 10, 0);
		for(int i = 10; i < 26; i++){
			buffer[i] = UlidTool.ENCODING_CHARS[0];
		}
		return new Ulid(new String(buffer));
	}

	public static Ulid createRandomUlidForTimestamp(long timestamp){
		return new Ulid(ULID_INSTANCE.nextULID(timestamp));
	}

	@Override
	public String toString(){
		return value;
	}

	@Override
	public int compareTo(Ulid other){
		return value.compareTo(other.value);
	}

	public String getAsHumanReadableTime(DateTimeFormatter formatter, ZoneId zoneId){
		var zonedDateTime = ZonedDateTime.ofInstant(getInstant(), zoneId);
		return formatter.format(zonedDateTime);
	}

}
