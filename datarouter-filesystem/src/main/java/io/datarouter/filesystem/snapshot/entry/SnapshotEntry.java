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
package io.datarouter.filesystem.snapshot.entry;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

public class SnapshotEntry{

	public static final Comparator<SnapshotEntry> KEY_COMPARATOR = Comparator.comparing(
			Function.identity(),
			(a, b) -> Arrays.compareUnsigned(
					a.keySlab, a.keyFrom, a.keyTo,
					b.keySlab, b.keyFrom, b.keyTo));

	private final byte[] keySlab;
	private final int keyFrom;
	private final int keyTo;

	private final byte[] valueSlab;
	private final int valueFrom;
	private final int valueTo;

	public final byte[][] columnValues;

	public SnapshotEntry(byte[] keySlab, byte[] valueSlab, byte[][] columnValues){
		this.keySlab = keySlab;
		keyFrom = 0;
		keyTo = keySlab.length;

		this.valueSlab = valueSlab;
		valueFrom = 0;
		valueTo = valueSlab.length;

		this.columnValues = columnValues;
	}

	public SnapshotEntry(
			byte[] key,
			int keyFrom,
			int keyTo,
			byte[] value,
			int valueFrom,
			int valueTo,
			byte[][] columnValues){
		this.keySlab = key;
		this.keyFrom = keyFrom;
		this.keyTo = keyTo;

		this.valueSlab = value;
		this.valueFrom = valueFrom;
		this.valueTo = valueTo;

		this.columnValues = columnValues;
	}

	/*------------- key --------------*/

	public byte[] keySlab(){
		return keySlab;
	}

	public int keyFrom(){
		return keyFrom;
	}

	public int keyTo(){
		return keyTo;
	}

	public int keyLength(){
		return keyTo - keyFrom;
	}

	public byte[] key(){
		if(keyFrom == 0 && keyTo == keySlab.length){
			return keySlab;
		}
		return Arrays.copyOfRange(keySlab, keyFrom, keyTo);
	}

	/*------------- value --------------*/

	public byte[] valueSlab(){
		return valueSlab;
	}

	public int valueFrom(){
		return valueFrom;
	}

	public int valueTo(){
		return valueTo;
	}

	public int valueLength(){
		return valueTo - valueFrom;
	}

	public byte[] value(){
		if(valueFrom == 0 && valueTo == valueSlab.length){
			return valueSlab;
		}
		return Arrays.copyOfRange(valueSlab, valueFrom, valueTo);
	}

	/*---------------- compare ------------*/

	public static boolean isSorted(SnapshotEntry first, SnapshotEntry second, boolean duplicatesAllowed){
		int diff = Arrays.compareUnsigned(
				first.keySlab, first.keyFrom, first.keyTo,
				second.keySlab, second.keyFrom, second.keyTo);
		if(duplicatesAllowed){
			return diff <= 0;
		}else{
			return diff < 0;
		}
	}

	/*------------- equals --------------*/

	public static boolean equal(SnapshotEntry left, SnapshotEntry right){
		return equalKeys(left, right) && equalColumnValues(left, right);
	}

	public static boolean equalKeys(SnapshotEntry left, SnapshotEntry right){
		return Arrays.equals(left.key(), right.key());
	}

	public static boolean equalColumnValues(SnapshotEntry left, SnapshotEntry right){
		if(left.columnValues.length != right.columnValues.length){
			return false;
		}
		for(int i = 0; i < left.columnValues.length; ++i){
			if(!equalColumnValue(left, right, i)){
				return false;
			}
		}
		return true;
	}

	public static boolean equalColumnValue(SnapshotEntry left, SnapshotEntry right, int valueIndex){
		return Arrays.equals(left.columnValues[valueIndex], right.columnValues[valueIndex]);
	}

}
