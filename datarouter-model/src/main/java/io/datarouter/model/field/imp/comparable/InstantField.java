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
package io.datarouter.model.field.imp.comparable;

import java.time.Instant;

import io.datarouter.bytes.codec.longcodec.RawLongCodec;
import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.model.util.FractionalSecondTool;

public class InstantField extends BasePrimitiveField<Instant,InstantFieldKey>{

	private static final RawLongCodec RAW_LONG_CODEC = RawLongCodec.INSTANCE;
	// Unfortunately this could have been 12.
	private static final int BYTES_LENGTH = 16;

	public InstantField(InstantFieldKey key, Instant value){
		super(key, FractionalSecondTool.truncate(value, key.getNumFractionalSeconds()));
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Instant parseStringEncodedValueButDoNotSet(String value){
		return Instant.parse(value);
	}

	@Override
	public byte[] getValueBytes(){
		if(value == null){
			return null;
		}
		return encodeToBytes(value);
	}

	@Override
	public int getApproximateValueBytesLength(){
		return value == null ? 0 : BYTES_LENGTH;
	}

	@Override
	public Instant fromValueBytesButDoNotSet(byte[] bytes, int offset){
		return decodeFromBytes(bytes, offset);
	}

	@Override
	public int numKeyBytesWithSeparator(byte[] bytes, int offset){
		return 16;
	}

	public static byte[] encodeToBytes(Instant value){
		byte[] bytes = new byte[BYTES_LENGTH];
		RAW_LONG_CODEC.encode(value.getEpochSecond(), bytes, 0);
		//nanos could fit in 4 bytes, but changing it would break persisted data
		RAW_LONG_CODEC.encode(value.getNano(), bytes, 8);
		return bytes;
	}

	public static Instant decodeFromBytes(byte[] bytes, int offset){
		long seconds = RAW_LONG_CODEC.decode(bytes, offset);
		long nanos = RAW_LONG_CODEC.decode(bytes, offset + 8);
		return Instant.ofEpochSecond(seconds, nanos);
	}

}
