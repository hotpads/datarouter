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
package io.datarouter.bytes;

import io.datarouter.bytes.codec.booleancodec.RawBooleanCodec;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;
import io.datarouter.bytes.codec.longcodec.RawLongCodec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;

public class ByteReader{

	private static final RawBooleanCodec RAW_BOOLEAN_CODEC = RawBooleanCodec.INSTANCE;
	private static final RawIntCodec RAW_INT_CODEC = RawIntCodec.INSTANCE;
	private static final ComparableLongCodec COMPARABLE_LONG_CODEC = ComparableLongCodec.INSTANCE;
	private static final RawLongCodec RAW_LONG_CODEC = RawLongCodec.INSTANCE;

	private final byte[] bytes;
	private int position;

	public ByteReader(byte[] bytes, int cursor){
		this.bytes = bytes;
		this.position = cursor;
	}

	public ByteReader(byte[] bytes){
		this(bytes, 0);
	}

	public int position(){
		return position;
	}

	public int skip(int numBytes){
		position += numBytes;
		return position;
	}

	public boolean hasMore(){
		return position < bytes.length;
	}

	public void assertFinished(){
		if(position != bytes.length){
			String message = String.format("position %s != bytes.length %s", position, bytes.length);
			throw new IllegalStateException(message);
		}
	}

	/*-------------- boolean -----------------*/

	public boolean booleanByte(){
		boolean value = RAW_BOOLEAN_CODEC.decode(bytes, position);
		++position;
		return value;
	}

	/*-------------- int -----------------*/

	public int skipInts(int num){
		return skip(num * Integer.BYTES);
	}

	public int rawInt(){
		int value = RAW_INT_CODEC.decode(bytes, position);
		position += Integer.BYTES;
		return value;
	}

	public int[] rawInts(int count){
		var value = new int[count];
		for(int i = 0; i < count; ++i){
			value[i] = rawInt();
		}
		return value;
	}

	public int varInt(){
		int value = VarIntTool.decodeInt(bytes, position);
		position += VarIntTool.length(value);
		return value;
	}

	public int[] varInts(int count){
		var value = new int[count];
		for(int i = 0; i < count; ++i){
			value[i] = varInt();
		}
		return value;
	}

	/*-------------- long -----------------*/

	public int skipLongs(int num){
		return skip(num * Long.BYTES);
	}

	public long comparableLong(){
		long value = COMPARABLE_LONG_CODEC.decode(bytes, position);
		position += Long.BYTES;
		return value;
	}

	public long rawLong(){
		long value = RAW_LONG_CODEC.decode(bytes, position);
		position += Long.BYTES;
		return value;
	}

	public long[] rawLongs(int count){
		var value = new long[count];
		for(int i = 0; i < count; ++i){
			value[i] = rawLong();
		}
		return value;
	}

	public long varLong(){
		long value = VarIntTool.decodeLong(bytes, position);
		position += VarIntTool.length(value);
		return value;
	}

	/*-------------- bytes --------------*/

	public byte[] bytes(int num){
		var value = ByteTool.copyOfRange(bytes, position, num);
		position += num;
		return value;
	}

	public byte[] varBytes(){
		int length = varInt();
		return bytes(length);
	}

	/*-------------- utf8 -----------------*/

	public String comparableUtf8(){
		int terminatorPosition = position;
		while(bytes[terminatorPosition] != 0){
			++terminatorPosition;
		}
		int length = terminatorPosition - position;
		String value = StringCodec.UTF_8.decode(bytes, position, length);
		position = terminatorPosition + 1;
		return value;
	}

	public String varUtf8(){
		byte[] bytes = varBytes();
		return StringCodec.UTF_8.decode(bytes);
	}

}
