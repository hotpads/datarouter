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

import java.util.Arrays;
import java.util.Objects;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;

/**
 * Equivalent to BlockfileRow but without the value field.
 *
 * The encoding should match BlockfileRow but without the value information.
 */
public class BlockfileRowVersion{

	private final byte[] bytes;
	private final int offset;
	private final int length;// Could be calculated but including it explicitly for now.
	private final int keyOffset;
	private final int keyLength;
	private final int versionOffset;
	private final int versionLength;
	private final BlockfileRowOp op;

	public BlockfileRowVersion(
			byte[] bytes,
			int offset,
			int length,
			int keyOffset,
			int keyLength,
			int versionOffset,
			int versionLength,
			BlockfileRowOp op){
		Objects.requireNonNull(bytes);
		Objects.requireNonNull(op);
		this.bytes = bytes;
		this.offset = offset;
		this.length = length;
		this.keyOffset = keyOffset;
		this.keyLength = keyLength;
		this.versionOffset = versionOffset;
		this.versionLength = versionLength;
		this.op = op;
	}

	public static BlockfileRowVersion create(byte[] key, byte[] version, BlockfileRowOp op){
		return BlockfileRowVersionCodec.create(key, version, op);
	}

	/*--------- convenience -------*/

	public static BlockfileRowVersion putWithoutVersion(
			byte[] key){
		return create(
				key,
				EmptyArray.BYTE,
				BlockfileRowOp.PUT);
	}

	/*--------- equals ---------*/

	public int compareKeyBytes(byte[] otherBytes, int otherOffset, int otherLength){
		return Arrays.compareUnsigned(
				bytes, keyOffset, keyOffset + keyLength,
				otherBytes, otherOffset, otherOffset + otherLength);
	}

	public boolean equalsKeyBytes(byte[] otherBytes, int otherOffset, int otherLength){
		return Arrays.equals(
				bytes, keyOffset, keyOffset + keyLength,
				otherBytes, otherOffset, otherOffset + otherLength);
	}

	public static boolean equalsKeyOptimized(BlockfileRowVersion left, BlockfileRowVersion right){
		//Skip null checks - nulls are rejected by the constructor
		//Skip == checks - they may share a common backing array at some point
		if(left.keyLength != right.keyLength){
			return false;
		}
		if(left.keyLength == 0){
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

	public static boolean equalsVersion(BlockfileRowVersion left, BlockfileRowVersion right){
		int leftTo = left.versionOffset + left.versionLength;
		int rightTo = right.versionOffset + right.versionLength;
		return Arrays.equals(left.bytes, left.versionOffset, leftTo, right.bytes, right.versionOffset, rightTo);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		BlockfileRowVersion other = (BlockfileRowVersion)obj;
		return Arrays.equals(
				bytes,
				offset,
				offset + length,
				other.bytes,
				other.offset,
				other.offset + other.length);
	}

	@Override
	public int hashCode(){
		int result = 1;
		for(int i = offset; i < length; ++i){
			result = 31 * result + bytes[i];
		}
		return result;
	}


	/*--------- compare ----------*/

	public int compareToKey(byte[] otherKey){
		return Arrays.compareUnsigned(
				bytes,
				keyOffset,
				keyOffset + keyLength,
				otherKey,
				0,
				otherKey.length);
	}

	public static int compareKey(BlockfileRowVersion left, BlockfileRowVersion right){
		return Arrays.compareUnsigned(
				left.bytes,
				left.keyOffset,
				left.keyOffset + left.keyLength,
				right.bytes,
				right.keyOffset,
				right.keyOffset + right.keyLength);
	}

	public static int compareVersion(BlockfileRowVersion left, BlockfileRowVersion right){
		return Arrays.compareUnsigned(
				left.bytes,
				left.versionOffset,
				left.versionOffset + left.versionLength,
				right.bytes,
				right.versionOffset,
				right.versionOffset + right.versionLength);
	}

	// Appears 15% faster than COMPARE_KEY.thenComparing(COMPARE_VERSION).thenComparing(COMPARE_OP)
	public static int compareKeyVersionOpOptimized(BlockfileRowVersion left, BlockfileRowVersion right){
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

	public int keyOffset(){
		return keyOffset;
	}

	public int keyLength(){
		return keyLength;
	}

	public byte[] copyOfKey(){
		return Arrays.copyOfRange(bytes, keyOffset, keyOffset + keyLength);
	}

	public int versionOffset(){
		return versionOffset;
	}

	public int versionLength(){
		return versionLength;
	}

	public byte[] copyOfVersion(){
		return Arrays.copyOfRange(bytes, versionOffset, versionOffset + versionLength);
	}

	public BlockfileRowOp op(){
		return op;
	}

	@Override
	public String toString(){
		return HexByteStringCodec.INSTANCE.encode(copyOfBytes());
	}

}
