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
package io.datarouter.scanner;

public class ArrayScanner<T> implements Scanner<T>{

	private final T[] array;
	private int index = -1;

	public ArrayScanner(T[] array){
		this.array = array;
	}

	public static <T> Scanner<T> of(T[] array){
		return array.length == 0 ? EmptyScanner.singleton() : new ArrayScanner<>(array);
	}

	@Override
	public boolean advance(){
		++index;
		if(index < array.length){
			return true;
		}
		return false;
	}

	@Override
	public T current(){
		return array[index];
	}

}
