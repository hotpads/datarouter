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

import java.nio.ByteBuffer;
import java.util.Arrays;

import io.datarouter.util.array.ArrayTool;

/**
 * lightweight, reusable class for specifying ranges of byte[]'s
 *
 * can contain convenience methods for comparing, printing, cloning,
 * spawning new arrays, copying to other arrays, etc.
 *  *
 */
public class ByteRange implements Comparable<ByteRange>{

	/********************** fields *****************************/

	//not making these final.  intention is to reuse objects of this class
	private byte[] bytes;
	private int offset;
	private int length;
	private int hash = 0;


	/********************** constructors ***********************/

	public ByteRange(byte[] bytes){
		set(bytes);
	}

	public ByteRange(byte[] bytes, int offset, int length){
		set(bytes, offset, length);
	}


	/********************** methods *************************/

	private ByteRange set(byte[] bytes){
		return set(bytes, 0, bytes.length);
	}

	private ByteRange set(byte[] bytes, int offset, int length){
		if(bytes == null){
			throw new NullPointerException("ByteRange does not support null bytes");
		}
		this.bytes = bytes;
		this.offset = offset;
		this.length = length;
		calculateHashCode();
		return this;
	}

	public byte[] copyToNewArray(){
		byte[] result = new byte[length];
		System.arraycopy(bytes, offset, result, 0, length);
		return result;
	}

	public byte[] copyToArrayNewArrayAndIncrement(){
		return ByteTool.unsignedIncrement(toArray());
	}

	public ByteRange cloneAndIncrement(){
		return new ByteRange(copyToArrayNewArrayAndIncrement());
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

	/******************* standard methods *********************/

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
		ByteRange that = (ByteRange)thatObject;
		return ByteTool.equals(bytes, offset, length, that.bytes, that.offset, that.length);
	}

	@Override
	public int hashCode(){
		return hash;
	}

	private void calculateHashCode(){
		if(ArrayTool.isEmpty(bytes)){
			hash = 0;
			return;
		}
		int off = offset;
		for(int i = 0; i < length; i++){
			hash = 31 * hash + bytes[off++];
		}
	}

	@Override
	public int compareTo(ByteRange other){
		return ByteTool.bitwiseCompare(bytes, offset, length,
				other.bytes, other.offset, other.length);
	}

	/******************** getters *****************************/

	public byte[] getBytes(){
		return bytes;
	}

	public int getLength(){
		return length;
	}

	@Override
	public String toString(){
		return Arrays.toString(bytes);
	}
}
