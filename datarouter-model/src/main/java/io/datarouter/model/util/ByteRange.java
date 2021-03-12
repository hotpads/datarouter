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
package io.datarouter.model.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.datarouter.util.bytes.ByteTool;

/**
 * lightweight, reusable class for specifying ranges of byte[]'s
 *
 * can contain convenience methods for comparing, printing, cloning,
 * spawning new arrays, copying to other arrays, etc.
 */
public class ByteRange implements Comparable<ByteRange>{

	private static final ByteRange EMPTY = new ByteRange(new byte[0]);

	// not making these final. intention is to reuse objects of this class
	private byte[] bytes;
	private int offset;
	private int length;

	public ByteRange(byte[] bytes){
		set(bytes);
	}

	public ByteRange(byte[] bytes, int offset){
		set(bytes, offset);
	}

	public ByteRange(byte[] bytes, int offset, int length){
		set(bytes, offset, length);
	}

	public static ByteRange empty(){
		return EMPTY;
	}

	private ByteRange set(byte[] bytes){
		return set(bytes, 0, bytes.length);
	}

	private ByteRange set(byte[] bytes, int offset){
		return set(bytes, offset, bytes.length - offset);
	}

	private ByteRange set(byte[] bytes, int offset, int length){
		if(bytes == null){
			throw new NullPointerException("ByteRange does not support null bytes");
		}
		this.bytes = bytes;
		this.offset = offset;
		this.length = length;
		return this;
	}

	public byte[] copyToNewArray(){
		byte[] result = new byte[length];
		System.arraycopy(bytes, offset, result, 0, length);
		return result;
	}

	public byte[] copyToNewArrayAndIncrement(){
		return ByteTool.unsignedIncrement(copyToNewArray());
	}

	private boolean isFullArray(){
		return offset == 0 && length == bytes.length;
	}

	public byte[] toArray(){
		return isFullArray() ? bytes : copyToNewArray();
	}

	public ByteBuffer getNewByteBuffer(){
		return ByteBuffer.wrap(bytes, offset, length);
	}

	@Override
	public boolean equals(Object thatObject){
		if(this == thatObject){
			return true;
		}
		if(!(thatObject instanceof ByteRange)){
			return false;
		}
		if(hashCode() != thatObject.hashCode()){
			return false;
		}
		ByteRange other = (ByteRange)thatObject;
		int thisTo = offset + length;
		int otherTo = other.offset + other.length;
		return Arrays.equals(bytes, offset, thisTo, other.bytes, other.offset, otherTo);
	}

	@Override
	public int hashCode(){
		int result = 1;
		for(int i = 0; i < length; ++i){
			result = 31 * result + bytes[offset + i];
		}
		return result;
	}

	@Override
	public int compareTo(ByteRange other){
		int thisTo = offset + length;
		int otherTo = other.offset + other.length;
		return Arrays.compareUnsigned(bytes, offset, thisTo, other.bytes, other.offset, otherTo);
	}

	public byte[] getBytes(){
		return bytes;
	}

	public int getOffset(){
		return offset;
	}

	public int getLength(){
		return length;
	}

	@Override
	public String toString(){
		return ByteTool.getIntString(copyToNewArray());
	}

	public String toUtf8String(){
		return new String(copyToNewArray(), StandardCharsets.UTF_8);
	}

}
