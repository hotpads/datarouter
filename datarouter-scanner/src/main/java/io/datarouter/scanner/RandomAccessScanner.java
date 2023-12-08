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
package io.datarouter.scanner;

import java.util.List;

/**
 * Avoid an intermediate Iterator when scanning a List with RandomAccess layout.
 */
public class RandomAccessScanner<T> implements Scanner<T>{

	private final List<T> list;
	private int index = -1;

	public RandomAccessScanner(List<T> list){
		this.list = list;
	}

	public static <T> Scanner<T> of(List<T> list){
		return list.isEmpty() ? EmptyScanner.singleton() : new RandomAccessScanner<>(list);
	}

	@Override
	public boolean advance(){
		++index;
		return index < list.size();
	}

	@Override
	public T current(){
		return list.get(index);
	}

}
