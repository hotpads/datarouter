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

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

public abstract class BasePrimitiveList<T> extends AbstractList<T>
implements RandomAccess{

	protected static final int NOT_FOUND_INDEX = -1;

	protected final int from;
	protected final int to;

	public BasePrimitiveList(int from, int to){
		this.from = from;
		this.to = to;
	}

	protected void initFromCollection(Collection<T> values){
		int cursor = 0;
		for(T value : values){
			set(cursor, value);
			++cursor;
		}
	}

	//subclasses can skip validation in these methods
	abstract int internalFirstIndexOf(Object object);
	abstract boolean internalEquals(Object object);
	abstract T internalGet(int index);
	abstract int internalHashCode();
	abstract boolean internalIsCorrectType(Object object);
	abstract int internalLastIndexOf(Object object);
	abstract T internalSet(int index, T value);
	abstract List<T> internalSubList(int fromIndex, int toIndex);

	@Override
	public boolean contains(Object obj){
		return internalIsCorrectType(obj)
			&& internalFirstIndexOf(obj) != NOT_FOUND_INDEX;
	}

	@Override
	public boolean equals(Object object){
		if(object == this){
			return true;
		}
		if(internalEquals(object)){
			return true;
		}
		return super.equals(object);
	}

	@Override
	public T get(int index){
		PrimitiveListTool.checkIndex(size(), index);
		return internalGet(index);
	}

	@Override
	public int hashCode(){
		return internalHashCode();
	}

	@Override
	public int indexOf(Object obj){
		if(internalIsCorrectType(obj)){
			int index = internalFirstIndexOf(obj);
			if(index >= 0){
				return index - from;
			}
		}
		return NOT_FOUND_INDEX;
	}

	@Override
	public int lastIndexOf(Object obj){
		if(internalIsCorrectType(obj)){
			int index = internalLastIndexOf(obj);
			if(index >= 0){
				return index - from;
			}
		}
		return NOT_FOUND_INDEX;
	}

	@Override
	public T set(int index, T value){
		PrimitiveListTool.checkIndex(size(), index);
		Objects.requireNonNull(value);
		return internalSet(index, value);
	}

	@Override
	public int size(){
		return to - from;
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex){
		PrimitiveListTool.checkSubListIndexes(size(), fromIndex, toIndex);
		if(fromIndex == toIndex){
			return List.of();
		}
		return internalSubList(fromIndex, toIndex);
	}

}
