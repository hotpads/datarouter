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

import java.util.Date;

import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;

public class ComparableDateCodec{

	public static final ComparableDateCodec INSTANCE = new ComparableDateCodec();

	private static final ComparableLongCodec LONG_CODEC = ComparableLongCodec.INSTANCE;
	private static final int LENGTH = LONG_CODEC.length();

	public int length(){
		return LENGTH;
	}

	public byte[] encode(Date value){
		var bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(Date value, byte[] bytes, int offset){
		LONG_CODEC.encode(value.getTime(), bytes, offset);
		return LENGTH;
	}

	public Date decode(byte[] bytes, int offset){
		long time = LONG_CODEC.decode(bytes, offset);
		return new Date(time);
	}

}
