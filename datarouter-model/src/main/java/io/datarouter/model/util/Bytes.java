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
package io.datarouter.model.util;

import java.util.Arrays;

import io.datarouter.util.bytes.ByteTool;

public class Bytes implements Comparable<Bytes>{

	public static final Bytes EMPTY = new Bytes(new byte[0]);

	private final byte[] bytes;
	private final int offset;
	private final int length;

	public Bytes(byte[] bytes){
		this(bytes, 0, bytes.length);
	}

	public Bytes(byte[] bytes, int offset, int length){
		this.bytes = bytes;
		this.offset = offset;
		this.length = length;
	}

	public int getLength(){
		return length;
	}

	public byte[] toArray(){
		byte[] result = new byte[length];
		System.arraycopy(bytes, offset, result, 0, length);
		return result;
	}

	@Override
	public boolean equals(Object thatObject){
		if(this == thatObject){
			return true;
		}
		if(!(thatObject instanceof Bytes)){
			return false;
		}
		if(hashCode() != thatObject.hashCode()){
			return false;
		}
		Bytes other = (Bytes)thatObject;
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
	public int compareTo(Bytes other){
		int thisTo = offset + length;
		int otherTo = other.offset + other.length;
		return Arrays.compareUnsigned(bytes, offset, thisTo, other.bytes, other.offset, otherTo);
	}

	@Override
	public String toString(){
		return ByteTool.getIntString(toArray());
	}

}
