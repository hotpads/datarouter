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
package io.datarouter.bytes;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * Concatenated List of byte arrays with RandomAccess
 */
public class ByteArrays extends AbstractList<byte[]>
implements RandomAccess{

	private static final ByteArrays EMPTY = ByteArrays.of(List.of());

	protected final byte[] backingArray;
	protected final int offset;// absolute offset of metadata in the backingArray
	protected final int valuesOffset;// absolute offset of values in the backingArray
	protected final int[] endings;// cache of the absolute offset of each value ending in the backingArray

	/*------------ static construct ------------*/

	public static ByteArrays empty(){
		return EMPTY;
	}

	public static ByteArrays of(List<byte[]> inputArrays){
		return new ByteArrays(inputArrays);
	}

	public static ByteArrays of(byte[] backingArray, int offset){
		return new ByteArrays(backingArray, offset);
	}

	/*--------- construct -----------*/

	protected ByteArrays(List<byte[]> items){
		Objects.requireNonNull(items);
		int size = items.size();
		List<byte[]> tokens = new ArrayList<>(1 + size + size);
		offset = 0;

		//build metadata
		int cursor = offset;
		tokens.add(VarIntTool.encode(size));
		cursor += VarIntTool.length(size);
		for(byte[] item : items){
			tokens.add(VarIntTool.encode(item.length));
			cursor += VarIntTool.length(item.length);
		}
		valuesOffset = cursor;

		//build endings
		endings = new int[size];
		int index = 0;
		for(byte[] item : items){
			cursor += item.length;
			endings[index] = cursor;
			++index;
		}

		//build backingArray
		tokens.addAll(items);
		backingArray = ByteTool.concat(tokens);
	}

	protected ByteArrays(byte[] backingArray, int offset){
		this.backingArray = backingArray;
		this.offset = offset;
		int cursor = offset;
		int size = VarIntTool.decodeInt(backingArray, cursor);
		cursor += VarIntTool.length(size);
		endings = new int[size];

		//temporarily store the length of each item in the endings array
		for(int i = 0; i < size; ++i){
			int length = VarIntTool.decodeInt(backingArray, cursor);
			cursor += VarIntTool.length(length);
			endings[i] = length;
		}

		valuesOffset = cursor;

		//endings are the end indexes (exclusive) of each item in the backingArray
		//we can overwrite the temporarily stored lengths in the endings array with the endings
		int endingCursor = cursor;
		for(int i = 0; i < size; ++i){
			int length = endings[i];
			endingCursor += length;
			endings[i] = endingCursor;
			cursor += length;
		}
	}

	/*------------- List ------------*/

	@Override
	public boolean contains(Object other){
		if(other instanceof byte[]){
			byte[] target = (byte[])other;
			int size = size();
			for(int i = 0; i < size; ++i){
				if(equalsItem(i, target)){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public byte[] get(int index){
		return Arrays.copyOfRange(backingArray, getFrom(index), getTo(index));
	}

	@Override
	public int size(){
		return endings.length;
	}

	/*---------- public -----------*/

	/**
	 * Number of bytes spanned in the backingArray
	 */
	public int getLength(){
		return getLastEnding() - offset;
	}

	/**
	 * Returns the backingArray directly if possible, otherwise a copy of the relevant range of bytes
	 */
	public byte[] toBytes(){
		return coversFullBackingArray()
				? backingArray
				: Arrays.copyOfRange(backingArray, offset, getLastEnding());
	}

	/*-------- protected ----------*/

	protected int getFrom(int index){
		return index == 0
				? valuesOffset
				: endings[index - 1];
	}

	protected int getTo(int index){
		return endings[index];
	}

	public int compareItem(int index, byte[] target){
		return Arrays.compareUnsigned(backingArray, getFrom(index), getTo(index), target, 0, target.length);
	}

	public static int compareItem(ByteArrays arrays1, int index1, ByteArrays arrays2, int index2){
		return Arrays.compareUnsigned(
				arrays1.backingArray, arrays1.getFrom(index1), arrays1.getTo(index1),
				arrays2.backingArray, arrays2.getFrom(index2), arrays2.getTo(index2));
	}

	protected boolean equalsItem(int index, byte[] target){
		return Arrays.equals(backingArray, getFrom(index), getTo(index), target, 0, target.length);
	}

	/*---------- private -----------*/

	private int getLastEnding(){
		return size() == 0
				? valuesOffset
				: endings[size() - 1];
	}

	private boolean coversFullBackingArray(){
		return offset == 0 && backingArray.length == getLastEnding();
	}

}
