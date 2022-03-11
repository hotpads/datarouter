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
package io.datarouter.bytes.codec.doublecodec;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.codec.longcodec.RawLongCodec;

public class NullableRawDoubleCodec{
	private static final Logger logger = LoggerFactory.getLogger(NullableRawDoubleCodec.class);

	public static final NullableRawDoubleCodec INSTANCE = new NullableRawDoubleCodec();

	private static final long NaN = 0x0010000000000000L;
	private static final long NULL = NaN;
	private static final RawDoubleCodec RAW_DOUBLE_CODEC = RawDoubleCodec.INSTANCE;
	private static final RawLongCodec RAW_LONG_CODEC = RawLongCodec.INSTANCE;
	private static final int LENGTH = RAW_DOUBLE_CODEC.length();
	private static final AtomicBoolean ENCODED_NULL = new AtomicBoolean(false);
	private static final AtomicBoolean DECODED_NULL = new AtomicBoolean(false);

	public int length(){
		return LENGTH;
	}

	public byte[] encode(Double value){
		if(value == null && !ENCODED_NULL.getAndSet(true)){
			logger.warn("", new Exception());
		}
		double nonNullValue = value == null ? Double.longBitsToDouble(NULL) : value;
		return RAW_DOUBLE_CODEC.encode(nonNullValue);
	}

	public double decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public Double decode(byte[] bytes, int offset){
		long nonNullValue = RAW_LONG_CODEC.decode(bytes, offset);
		Double nullableValue = nonNullValue == NULL ? null : Double.longBitsToDouble(nonNullValue);
		if(nullableValue == null && !DECODED_NULL.getAndSet(true)){
			logger.warn("", new Exception());
		}
		return nullableValue;
	}

}
