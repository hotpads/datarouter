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
package io.datarouter.util;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;

import de.huxhorn.sulky.ulid.ULID;

public record Ulid(String value) implements Comparable<Ulid>{

	public static final int LENGTH = 26;

	private static final char[] ENCODING_CHARS = {
			'0','1','2','3','4','5','6','7','8','9',
			'A','B','C','D','E','F','G','H','J','K',
			'M','N','P','Q','R','S','T','V','W','X',
			'Y','Z',
	};
	private static final int MASK = 0x1F;
	private static final int MASK_BITS = 5;
	private static final long TIMESTAMP_MASK = 0x0000_FFFF_FFFF_FFFFL;

	public Ulid{
		Objects.requireNonNull(value);
		ULID.parseULID(value);
	}

	public Ulid(){
		this(new ULID().nextULID());
	}

	public long getTimestampMs(){
		return ULID.parseULID(value).timestamp();
	}

	public Instant getInstant(){
		return Instant.ofEpochMilli(getTimestampMs());
	}

	public static Ulid createFirstUlidFromTimestamp(long timestamp){
		timestamp = timestamp & TIMESTAMP_MASK;

		char[] buffer = new char[26];

		internalWriteCrockford(buffer, timestamp, 10, 0);
		for(int i = 10; i < 26; i++){
			buffer[i] = ENCODING_CHARS[0];
		}
		return new Ulid(new String(buffer));
	}

	public static Ulid createUlidFromTimestamp(long timestamp, long seed){
		Random random = new Random(seed);
		timestamp = timestamp & TIMESTAMP_MASK;

		char[] buffer = new char[26];

		internalWriteCrockford(buffer, timestamp, 10, 0);
		internalWriteCrockford(buffer, random.nextLong(), 8, 10);
		internalWriteCrockford(buffer, random.nextLong(), 8, 18);
		return new Ulid(new String(buffer));
	}

	private static void internalWriteCrockford(char[] buffer, long value, int count, int offset){
		for(int i = 0; i < count; i++){
			int index = (int)((value >>> ((count - i - 1) * MASK_BITS)) & MASK);
			buffer[offset + i] = ENCODING_CHARS[index];
		}
	}

	@Override
	public int compareTo(Ulid other){
		return value.compareTo(other.value);
	}

}
