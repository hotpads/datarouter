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
package io.datarouter.bytes.kvfile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.InputStreamTool;
import io.datarouter.bytes.MultiByteArrayInputStream;
import io.datarouter.bytes.VarIntTool;
import io.datarouter.scanner.Scanner;

public class KvFileEntrySerializer{

	/*------------ encode one ------------*/

	public static byte[] toBytes(
			byte[] key,
			byte[] version,
			KvFileOp op,
			byte[] value){
		byte[][] tokens = {
				EmptyArray.BYTE,//placeholder for length
				VarIntTool.encode(key.length),
				key,
				VarIntTool.encode(version.length),
				version,
				op.persistentValueArray,
				VarIntTool.encode(value.length),
				value
		};
		tokens[0] = VarIntTool.encode(ByteTool.totalLength(tokens));
		return ByteTool.concat(tokens);
	}

	/*----------- encode multi -----------*/

	public static InputStream toInputStream(Scanner<KvFileEntry> entries){
		return entries
				.map(KvFileEntry::bytes)
				.apply(MultiByteArrayInputStream::new);
	}

	/*------------ decode one ------------*/

	public static KvFileEntry fromBytes(byte[] bytes){
		int cursor = 0;

		int length = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(length);

		int keyLength = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(keyLength);
		int keyOffset = cursor;
		cursor += keyLength;

		int versionLength = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(versionLength);
		int versionOffset = cursor;
		cursor += versionLength;

		KvFileOp op = KvFileOp.fromByte(bytes[cursor]);
		++cursor;

		int valueLength = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(valueLength);
		int valueOffset = cursor;
		cursor += valueLength;

		return new KvFileEntry(
				bytes,
				keyOffset, keyLength,
				versionOffset, versionLength,
				op,
				valueOffset, valueLength);
	}

	// Null indicates the end of the stream
	public static byte[] entryBytesFromInputStream(InputStream inputStream){
		Integer numDataBytes = VarIntTool.decodeIntOrNull(inputStream);
		if(numDataBytes == null){
			return null;
		}
		int numLengthBytes = VarIntTool.length(numDataBytes);
		int totalLength = numLengthBytes + numDataBytes;
		byte[] bytes = new byte[totalLength];
		VarIntTool.encode(bytes, 0, numDataBytes);
		InputStreamTool.readUntilLength(inputStream, bytes, numLengthBytes, numDataBytes);
		return bytes;
	}

	// Null indicates the end of the stream
	public static KvFileEntry entryFromInputStream(InputStream inputStream){
		byte[] entryBytes = entryBytesFromInputStream(inputStream);
		return entryBytes == null ? null : fromBytes(entryBytes);
	}

	/*-------------- decodeMulti ------------*/

	public static Scanner<KvFileEntry> decodeMulti(byte[] bytes){
		//TODO parse bytes directly
		var inputStream = new ByteArrayInputStream(bytes);
		return Scanner.generate(() -> entryFromInputStream(inputStream))
				.advanceUntil(Objects::isNull);
	}

	public static Scanner<KvFileEntry> decodeMulti(InputStream inputStream){
		return Scanner.generate(() -> entryFromInputStream(inputStream))
				.advanceUntil(Objects::isNull);
	}

}
