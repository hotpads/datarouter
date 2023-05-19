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
package io.datarouter.bytes.primitivelist;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.datarouter.bytes.EmptyArray;

/**
 * Provides a view over a subset of a primitive float array with List&#60;Float&#62; semantics.
 * List elements can be modified, but the list can't change size.
 * Nulls are rejected.
 */
public class PrimitiveFloatList extends BasePrimitiveList<Float>{

	private static final PrimitiveFloatList EMPTY = new PrimitiveFloatList(EmptyArray.FLOAT);

	private final float[] array;

	public PrimitiveFloatList(float[] array){
		this(array, 0, array.length);
	}

	public PrimitiveFloatList(float[] array, int from, int to){
		super(from, to);
		this.array = array;
	}

	public PrimitiveFloatList(Collection<Float> values){
		super(0, values.size());
		array = new float[values.size()];
		initFromCollection(values);
	}

	public static PrimitiveFloatList empty(){
		return EMPTY;
	}

	/*------------ internal --------------*/

	@Override
	protected boolean internalEquals(Object object){
		if(object instanceof PrimitiveFloatList that){
			return Arrays.equals(array, from, to, that.array, that.from, that.to);
		}
		return false;
	}

	@Override
	protected int internalFirstIndexOf(Object obj){
		float value = (Float)obj;
		for(int i = from; i < to; i++){
			if(array[i] == value){
				return i;
			}
		}
		return NOT_FOUND_INDEX;
	}

	@Override
	protected Float internalGet(int index){
		return array[from + index];
	}

	@Override
	protected int internalHashCode(){
		int result = 1;
		for(int i = from; i < to; i++){
			result = 31 * result + Float.floatToIntBits(array[i]);
		}
		return result;
	}

	@Override
	protected boolean internalIsCorrectType(Object object){
		return object instanceof Float;
	}

	@Override
	protected int internalLastIndexOf(Object obj){
		float value = (Float)obj;
		for(int i = to - 1; i >= from; i--){
			if(array[i] == value){
				return i;
			}
		}
		return NOT_FOUND_INDEX;
	}

	@Override
	protected Float internalSet(int index, Float value){
		float oldValue = array[index];
		array[from + index] = value;
		return oldValue;
	}

	@Override
	protected List<Float> internalSubList(int fromIndex, int toIndex){
		return new PrimitiveFloatList(array, from + fromIndex, from + toIndex);
	}

}
