/**
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
package io.datarouter.util.bytes;

import java.nio.charset.StandardCharsets;

public class ByteReader{

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

	/*-------------- int -----------------*/

	public int skipInts(int num){
		return skip(num * 4);
	}

	public int rawInt(){
		int value = IntegerByteTool.fromRawBytes(bytes, position);
		position += 4;
		return value;
	}

	public int[] rawInts(int count){
		int[] value = new int[count];
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
		int[] value = new int[count];
		for(int i = 0; i < count; ++i){
			value[i] = varInt();
		}
		return value;
	}

	/*-------------- long -----------------*/

	public int skipLongs(int num){
		return skip(num * 8);
	}

	public long rawLong(){
		long value = LongByteTool.fromRawBytes(bytes, position);
		position += 8;
		return value;
	}

	public long varLong(){
		long value = VarIntTool.decodeLong(bytes, position);
		position += VarIntTool.length(value);
		return value;
	}

	/*-------------- bytes --------------*/

	public byte[] bytes(int num){
		byte[] value = ByteTool.copyOfRange(bytes, position, num);
		position += num;
		return value;
	}

	public byte[] varBytes(){
		int length = varInt();
		return bytes(length);
	}

	/*-------------- utf8 -----------------*/

	public String varUtf8(){
		byte[] bytes = varBytes();
		return new String(bytes, StandardCharsets.UTF_8);
	}

}
