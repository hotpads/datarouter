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
package io.datarouter.bytes.codec.longcodec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated//use ComparableLongCodec or RawLongCodec
public class UInt63Codec{
	private static final Logger logger = LoggerFactory.getLogger(UInt63Codec.class);

	public static final UInt63Codec INSTANCE = new UInt63Codec();

	private static final RawLongCodec RAW_CODEC = RawLongCodec.INSTANCE;
	private static final int LENGTH = RAW_CODEC.length();

	public int length(){
		return LENGTH;
	}

	public byte[] encode(long value){
		var bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(long value, byte[] bytes, int offset){
		if(value < 0 && value != Long.MIN_VALUE){//need to allow Long.MIN_VALUE in for nulls
			//TODO throw error
			logger.warn("", new IllegalArgumentException("no negatives: " + value));
		}
		return RAW_CODEC.encode(value, bytes, offset);
	}

	public long decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public long decode(byte[] bytes, int offset){
		long value = RAW_CODEC.decode(bytes, offset);
		if(value < 0 && value != Long.MIN_VALUE){
			//TODO throw error
			logger.warn("", new IllegalArgumentException("no negatives: " + value));
		}
		return value;
	}

}
