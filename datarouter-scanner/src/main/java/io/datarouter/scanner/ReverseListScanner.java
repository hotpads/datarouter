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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Avoid an intermediate Iterator when scanning a List with RandomAccess layout.
 */
public class ReverseListScanner<T> implements Scanner<T>{

	private final List<T> list;
	private int index;

	public ReverseListScanner(List<T> list){
		this.list = list;
		index = list.size();
	}

	public static <T> Scanner<T> of(List<T> list){
		if(list.size() == 0){
			return EmptyScanner.singleton();
		}
		if(list instanceof LinkedList){
			LinkedList<T> linkedList = (LinkedList<T>)list;
			Iterator<T> descendingIterator = linkedList.descendingIterator();
			return IteratorScanner.of(descendingIterator);
		}
		return new ReverseListScanner<>(list);
	}

	@Override
	public boolean advance(){
		--index;
		return index >= 0;
	}

	@Override
	public T current(){
		return list.get(index);
	}

}
