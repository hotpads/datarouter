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

import java.util.Arrays;
import java.util.Objects;

import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;
import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;

/**
 * A generic KeyValue, plus a version represented as bytes.  Entries are compared by key+version.
 *
 * Empty fields:
 * - A key is generally required for any useful workload.
 * - The version can be an empty byte[] if keys are unique.
 * - The value can be an empty byte[] if all information is contained in the key.
 *
 * The bytes are interpreted lexicographically using Arrays::compareUnsigned.
 *
 * Many of these can be written to an OutputStream to be persisted for backup or further processing.
 *
 * Encodes KvFileEntry objects to bytes using the format:
 * - varInt: total length of the all following fields
 *   - the total length is redundant, but can help split the InputStream faster for passing decoding to other threads
 *   - it can also help identify corruption, if the lengths don't add up
 * - varInt: keyLength
 * - bytes: key
 * - varInt: versionLength
 * - bytes: version
 * - byte: opType
 * - varInt: valueLength
 * - bytes: value
 */
public class KvFileEntry{

	private final byte[] bytes;
	private final int offset;
	private final int length;// Could be calculated but including it explicitly for now.
	private final int keyOffset;
	private final int keyLength;
	private final int versionOffset;
	private final int versionLength;
	private final KvFileOp op;
	private final int valueOffset;
	private final int valueLength;

	public KvFileEntry(
			byte[] bytes,
			int offset,
			int length,
			int keyOffset,
			int keyLength,
			int versionOffset,
			int versionLength,
			KvFileOp op,
			int valueOffset,
			int valueLength){
		Objects.requireNonNull(bytes);
		Objects.requireNonNull(op);
		if(op == KvFileOp.DELETE && valueLength != 0){
			throw new IllegalArgumentException("Cannot have value with delete op");
		}
		this.bytes = bytes;
		this.offset = offset;
		this.length = length;
		this.keyOffset = keyOffset;
		this.keyLength = keyLength;
		this.versionOffset = versionOffset;
		this.versionLength = versionLength;
		this.op = op;
		this.valueOffset = valueOffset;
		this.valueLength = valueLength;
		if(length != valueOffset + valueLength - offset){
			String message = String.format(
					"length[%s] != valueOffset[%s]+valueLength[%s]",
					length,
					valueOffset,
					valueLength);
			throw new IllegalArgumentException(message);
		}
	}

	public static KvFileEntry create(byte[] key, byte[] version, KvFileOp op, byte[] value){
		byte[] bytes = KvFileEntrySerializer.toBytes(key, version, op, value);
		//TODO perhaps that should build the entry directly rather than parsing the bytes
		return KvFileEntrySerializer.fromBytes(bytes);
	}

	/*--------- convenience -------*/

	public static KvFileEntry putWithLongVersion(
			byte[] key,
			long version,
			byte[] value){
		return create(
				key,
				ComparableLongCodec.INSTANCE.encode(version),
				KvFileOp.PUT,
				value);
	}


	/*--------- equals ---------*/

	public static boolean equalsKeyOptimized(KvFileEntry left, KvFileEntry right){
		//Skip null checks - nulls are rejected by the constructor
		//Skip == checks - they may sharing a common backing array at some point
		if(left.keyLength != right.keyLength){
			return false;
		}
		int length = left.keyLength;
		if(length == 0){
			return true;
		}

		int leftTo = left.keyOffset + left.keyLength;
		int rightTo = right.keyOffset + right.keyLength;

		//Many of our keys will share common prefixes, so we look for a mismatch at the end first.
		if(left.bytes[leftTo - 1] != right.bytes[rightTo - 1]){
			return false;
		}
		return Arrays.equals(left.bytes, left.keyOffset, leftTo, right.bytes, right.keyOffset, rightTo);
	}

	public static boolean equalsVersion(KvFileEntry left, KvFileEntry right){
		int leftTo = left.versionOffset + left.versionLength;
		int rightTo = right.versionOffset + right.versionLength;
		return Arrays.equals(left.bytes, left.versionOffset, leftTo, right.bytes, right.versionOffset, rightTo);

	}

	/*--------- compare ----------*/

	public static int compareKey(KvFileEntry left, KvFileEntry right){
		return Arrays.compareUnsigned(
				left.bytes,
				left.keyOffset,
				left.keyOffset + left.keyLength,
				right.bytes,
				right.keyOffset,
				right.keyOffset + right.keyLength);
	}

	public static int compareVersion(KvFileEntry left, KvFileEntry right){
		return Arrays.compareUnsigned(
				left.bytes,
				left.versionOffset,
				left.versionOffset + left.versionLength,
				right.bytes,
				right.versionOffset,
				right.versionOffset + right.versionLength);
	}

	// Appears 15% faster than COMPARE_KEY.thenComparing(COMPARE_VERSION).thenComparing(COMPARE_OP)
	public static int compareKeyVersionOpOptimized(KvFileEntry left, KvFileEntry right){
		int keyDiff = compareKey(left, right);
		if(keyDiff != 0){
			return keyDiff;
		}
		int versionDiff = compareVersion(left, right);
		if(versionDiff != 0){
			return versionDiff;
		}
		return left.op.compareTo(right.op);
	}

	/*------- get -------*/

	public byte[] backingBytes(){
		return bytes;
	}

	public byte[] copyOfBytes(){
		return Arrays.copyOfRange(bytes, offset, offset + length);
	}

	public int offset(){
		return offset;
	}

	public int length(){
		return length;
	}

	public byte[] copyOfKey(){
		return Arrays.copyOfRange(bytes, keyOffset, keyOffset + keyLength);
	}

	public byte[] copyOfVersion(){
		return Arrays.copyOfRange(bytes, versionOffset, versionOffset + versionLength);
	}

	public KvFileOp op(){
		return op;
	}

	public byte[] copyOfValue(){
		return Arrays.copyOfRange(bytes, valueOffset, valueOffset + valueLength);
	}

	@Override
	public String toString(){
		return HexByteStringCodec.INSTANCE.encode(copyOfBytes());
	}

}
