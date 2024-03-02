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
package io.datarouter.bytes.blockfile.row;

import io.datarouter.bytes.varint.VarIntTool;

/**
 * Format:
 * [keyLength] varint
 * [keyBytes] bytes
 * [versionLength] varint
 * [versionBytes] bytes
 * [op] single byte
 * [valueLength] varint
 * [valueBytes] bytes
 */
public class BlockfileRowCodec{

	/*------------ length ------------*/

	public static int length(
			byte[] key,
			byte[] version,
			byte[] value){
		return VarIntTool.length(key.length) + key.length
				+ VarIntTool.length(version.length) + version.length
				+ BlockfileRowOp.NUM_PERSISTENT_BYTES
				+ VarIntTool.length(value.length) + value.length;
	}

	/*------------ encode ------------*/

	public static BlockfileRow create(
			byte[] key,
			byte[] version,
			BlockfileRowOp op,
			byte[] value){
		// calculate offsets
		int keyLengthOffset = 0;
		int keyOffset = keyLengthOffset + VarIntTool.length(key.length);
		int versionLengthOffset = keyOffset + key.length;
		int versionOffset = versionLengthOffset + VarIntTool.length(version.length);
		int opOffset = versionOffset + version.length;
		int valueLengthOffset = opOffset + BlockfileRowOp.NUM_PERSISTENT_BYTES;
		int valueOffset = valueLengthOffset + VarIntTool.length(value.length);
		int length = valueOffset + value.length;

		// populate array
		byte[] bytes = new byte[length];
		encodeInto(key, version, op, value, bytes, 0);

		// create row object
		return new BlockfileRow(
				bytes, 0, length,
				keyOffset, key.length,
				versionOffset, version.length,
				op,
				valueOffset, value.length);
	}

	public static byte[] encode(
			byte[] key,
			byte[] version,
			BlockfileRowOp op,
			byte[] value){
		int length = length(key, version, value);
		byte[] intoBytes = new byte[length];
		encodeInto(key, version, op, value, intoBytes, 0);
		return intoBytes;
	}

	public static void encodeInto(
			byte[] key,
			byte[] version,
			BlockfileRowOp op,
			byte[] value,
			byte[] intoBytes,
			int offset){
		int cursor = offset;
		cursor += VarIntTool.encode(intoBytes, cursor, key.length);
		System.arraycopy(key, 0, intoBytes, cursor, key.length);
		cursor += key.length;
		cursor += VarIntTool.encode(intoBytes, cursor, version.length);
		System.arraycopy(version, 0, intoBytes, cursor, version.length);
		cursor += version.length;
		intoBytes[cursor] = op.persistentValue;
		++cursor;
		cursor += VarIntTool.encode(intoBytes, cursor, value.length);
		System.arraycopy(value, 0, intoBytes, cursor, value.length);
		cursor += value.length;
	}

	/*------------ decode ------------*/

	public static BlockfileRow fromBytes(byte[] bytes){
		return fromBytes(bytes, 0);
	}

	public static BlockfileRow fromBytes(byte[] bytes, int offset){
		int cursor = offset;

		int keyLength = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(keyLength);
		int keyOffset = cursor;
		cursor += keyLength;

		int versionLength = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(versionLength);
		int versionOffset = cursor;
		cursor += versionLength;

		BlockfileRowOp op = BlockfileRowOp.fromByte(bytes[cursor]);
		++cursor;

		int valueLength = 0;
		int valueOffset = cursor;
		valueLength = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(valueLength);
		valueOffset = cursor;
		cursor += valueLength;

		int length = cursor - offset;
		return new BlockfileRow(
				bytes,
				offset,
				length,
				keyOffset,
				keyLength,
				versionOffset,
				versionLength,
				op,
				valueOffset,
				valueLength);
	}

}
