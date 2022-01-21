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
package io.datarouter.bytes.codec.stringcodec;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.VarIntTool;

/**
 * Encodes a String as UTF-8 bytes prefixed with the number of bytes encoded as a VarInt.
 */
public class PrefixedStringCodec{

	public static final PrefixedStringCodec US_ASCII = new PrefixedStringCodec(StringCodec.US_ASCII);
	public static final PrefixedStringCodec ISO_8859_1 = new PrefixedStringCodec(StringCodec.ISO_8859_1);
	public static final PrefixedStringCodec UTF_8 = new PrefixedStringCodec(StringCodec.UTF_8);

	private final StringCodec stringCodec;

	public PrefixedStringCodec(StringCodec stringCodec){
		this.stringCodec = stringCodec;
	}

	public byte[] encode(String value){
		byte[] valueBytes = stringCodec.encode(value);
		byte[] lengthBytes = VarIntTool.encode(valueBytes.length);
		return ByteTool.concat(lengthBytes, valueBytes);
	}

	public int encode(String value, byte[] bytes, int offset){
		byte[] valueBytes = stringCodec.encode(value);
		byte[] lengthBytes = VarIntTool.encode(valueBytes.length);
		int cursor = offset;
		System.arraycopy(lengthBytes, 0, bytes, cursor, lengthBytes.length);
		cursor += lengthBytes.length;
		System.arraycopy(valueBytes, 0, bytes, offset, valueBytes.length);
		cursor += valueBytes.length;
		return cursor - offset;
	}

	public String decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public String decode(byte[] bytes, int offset){
		return decodeWithLength(bytes, offset).value;
	}

	public LengthAndValue<String> decodeWithLength(byte[] bytes, int offset){
		int cursor = offset;
		int valueBytesLength = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(valueBytesLength);
		String value = stringCodec.decode(bytes, cursor, valueBytesLength);
		cursor += valueBytesLength;
		int length = cursor - offset;
		return new LengthAndValue<>(length, value);
	}

}
