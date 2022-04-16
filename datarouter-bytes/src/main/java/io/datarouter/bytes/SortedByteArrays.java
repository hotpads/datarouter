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

import java.util.Arrays;
import java.util.List;

import io.datarouter.bytes.codec.bytestringcodec.CsvIntByteStringCodec;

/**
 * Restricts the input arrays to being ordered lexicographically, enabling binary search.
 */
public class SortedByteArrays extends ByteArrays{

	/*------------ static construct from individual arrays ------------*/

	public static SortedByteArrays ofIndividualArrays(List<byte[]> inputArrays){
		requireOrderedList(inputArrays);
		return new SortedByteArrays(inputArrays);
	}

	public static SortedByteArrays ofIndividualArraysNoValidation(List<byte[]> inputArrays){
		return new SortedByteArrays(inputArrays);
	}

	/*------------ static construct from ByteArrays ------------*/

	public static SortedByteArrays ofByteArrays(ByteArrays byteArrays){
		requireOrderedByteArrays(byteArrays);
		return ofByteArraysNoValidation(byteArrays);
	}

	public static SortedByteArrays ofByteArraysNoValidation(ByteArrays byteArrays){
		return new SortedByteArrays(byteArrays.backingArray, byteArrays.offset);
	}

	/*------------ construct ------------*/

	private SortedByteArrays(List<byte[]> inputArrays){
		super(inputArrays);
	}

	private SortedByteArrays(byte[] backingArray, int offset){
		super(backingArray, offset);
	}

	/*------------- List -------------*/

	@Override
	public boolean contains(Object other){
		if(other instanceof byte[]){
			byte[] target = (byte[])other;
			return binarySearch(target) >= 0;
		}
		return false;
	}

	/*----------- private ----------*/

	/**
	 * @param target the item to be searched for
	 * @return index of the search item if it's contained in the array;
	 * 		   	otherwise <code>(-(<i>insertion point</i>) - 1)</code>. The <i>insertion point</i> is defined as
	 * 			the point at which the key would be inserted into the array i.e. the index of the first element that's
	 * 			greater than target.
	 */
	private int binarySearch(byte[] target){
		int start = 0;
		int end = size() - 1;
		if(end < 0){
			return -1;
		}
		while(end >= start){
			int middle = (start + end) >>> 1;
			int sign = compareItem(middle, target);
			if(sign > 0){
				end = middle - 1;
			}else if(sign < 0){
				start = middle + 1;
			}else{
				return middle;
			}
		}
		return -(start + 1); // -(1 + val) == insertion point
	}

	private int compareItem(int index, byte[] target){
		return Arrays.compareUnsigned(backingArray, getFrom(index), getTo(index), target, 0, target.length);
	}

	private static int compareItem(ByteArrays arrays1, int index1, ByteArrays arrays2, int index2){
		return Arrays.compareUnsigned(
				arrays1.backingArray, arrays1.getFrom(index1), arrays1.getTo(index1),
				arrays2.backingArray, arrays2.getFrom(index2), arrays2.getTo(index2));
	}

	/*------------- validate individual arrays ----------------*/

	private static List<byte[]> requireOrderedList(List<byte[]> arrays){
		if(arrays.size() <= 1){
			return arrays;
		}
		for(int i = 1; i < arrays.size(); ++i){
			byte[] first = arrays.get(i - 1);
			byte[] second = arrays.get(i);
			if(Arrays.compareUnsigned(first, second) > 0){
				String message = String.format("[%s] is after [%s]",
						CsvIntByteStringCodec.INSTANCE.encode(first),
						CsvIntByteStringCodec.INSTANCE.encode(second));
				throw new IllegalArgumentException(message);
			}
		}
		return arrays;
	}

	/*------------- validate ByteArrays ----------------*/

	private static ByteArrays requireOrderedByteArrays(ByteArrays arrays){
		if(arrays.size() <= 1){
			return arrays;
		}
		for(int i = 1; i < arrays.size(); ++i){
			int firstIndex = i - 1;
			int secondIndex = i;
			if(compareItem(arrays, firstIndex, arrays, secondIndex) > 0){
				String message = String.format("[%s] is after [%s]",
						CsvIntByteStringCodec.INSTANCE.encode(arrays.get(firstIndex)),
						CsvIntByteStringCodec.INSTANCE.encode(arrays.get(secondIndex)));
				throw new IllegalArgumentException(message);
			}
		}
		return arrays;
	}

}
