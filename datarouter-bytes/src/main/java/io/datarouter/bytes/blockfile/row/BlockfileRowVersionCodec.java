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

public class BlockfileRowVersionCodec{

	/*------------ length ------------*/

	public static int length(
			byte[] key,
			byte[] version){
		return VarIntTool.length(key.length) + key.length
				+ VarIntTool.length(version.length) + version.length
				+ BlockfileRowOp.NUM_PERSISTENT_BYTES;
	}

	/*------------ encode ------------*/

	public static BlockfileRowVersion create(
			byte[] key,
			byte[] version,
			BlockfileRowOp op){
		// calculate offsets
		int keyLengthOffset = 0;
		int keyOffset = keyLengthOffset + VarIntTool.length(key.length);
		int versionLengthOffset = keyOffset + key.length;
		int versionOffset = versionLengthOffset + VarIntTool.length(version.length);
		int opOffset = versionOffset + version.length;
		int length = opOffset + BlockfileRowOp.NUM_PERSISTENT_BYTES;

		// populate array
		byte[] bytes = new byte[length];
		encodeInto(key, version, op, bytes, 0);

		// create row object
		return new BlockfileRowVersion(
				bytes, 0, length,
				keyOffset, key.length,
				versionOffset, version.length,
				op);
	}

	public static byte[] encode(
			byte[] key,
			byte[] version,
			BlockfileRowOp op){
		int length = length(key, version);
		byte[] intoBytes = new byte[length];
		encodeInto(key, version, op, intoBytes, 0);
		return intoBytes;
	}

	public static void encodeInto(
			byte[] key,
			byte[] version,
			BlockfileRowOp op,
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
	}

	/*------------ decode ------------*/

	public static BlockfileRowVersion fromBytes(byte[] bytes, int offset){
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

		int length = cursor - offset;
		return new BlockfileRowVersion(
				bytes,
				offset,
				length,
				keyOffset,
				keyLength,
				versionOffset,
				versionLength,
				op);
	}

	public static BlockfileRowVersion fromBytes(byte[] bytes){
		return fromBytes(bytes, 0);
	}

}
