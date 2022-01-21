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
package io.datarouter.bytes.codec.time;

import java.time.Instant;

import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;

public class ComparableInstantCodec{

	public static final ComparableInstantCodec INSTANCE = new ComparableInstantCodec();

	private static final ComparableLongCodec LONG_CODEC = ComparableLongCodec.INSTANCE;
	private static final ComparableIntCodec INT_CODEC = ComparableIntCodec.INSTANCE;
	private static final int LENGTH = LONG_CODEC.length() + INT_CODEC.length();

	public int length(){
		return LENGTH;
	}

	public byte[] encode(Instant value){
		var bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(Instant value, byte[] bytes, int offset){
		int cursor = offset;
		LONG_CODEC.encode(value.getEpochSecond(), bytes, cursor);
		cursor += LONG_CODEC.length();
		INT_CODEC.encode(value.getNano(), bytes, cursor);
		return LENGTH;
	}

	public Instant decode(byte[] bytes, int offset){
		int cursor = offset;
		long epochSeconds = LONG_CODEC.decode(bytes, cursor);
		cursor += LONG_CODEC.length();
		int nanos = INT_CODEC.decode(bytes, cursor);
		return Instant.ofEpochSecond(epochSeconds, nanos);
	}

}
