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
package io.datarouter.bytes.kvfile.kv;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.varint.VarIntTool;

public class KvFileEntryCodec{

	/*------------ encode one ------------*/

	public static byte[] toBytes(
			byte[] key,
			byte[] version,
			KvFileOp op,
			byte[] value){
		return ByteTool.concat(
				VarIntTool.encode(key.length),
				key,
				VarIntTool.encode(version.length),
				version,
				op.persistentValueArray,
				VarIntTool.encode(value.length),
				value);
	}

	/*------------ decode one ------------*/

	public static KvFileEntry fromBytes(byte[] bytes, int offset){
		int cursor = offset;

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

		int length = cursor - offset;
		return new KvFileEntry(
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

	public static KvFileEntry fromBytes(byte[] bytes){
		return fromBytes(bytes, 0);
	}

}
