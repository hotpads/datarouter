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
package io.datarouter.util.iterable.scanner.imp;

import java.util.ArrayList;
import java.util.Objects;

import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.scanner.Scanner;

/**
 * Users of this class can assume it will keep RandomAccess performance, currently implemented with ArrayList
 */
public class ListBackedScanner<T> implements Scanner<T>{

	private final ArrayList<T> items;
	private int currentIndex;

	public ListBackedScanner(ArrayList<T> items){
		this.items = Objects.requireNonNull(items);
		this.currentIndex = -1;
	}

	public static <T> ListBackedScanner<T> ofIterable(Iterable<T> items){
		Objects.requireNonNull(items);
		ArrayList<T> arrayList;
		if(items instanceof ArrayList){
			arrayList = (ArrayList<T>)items;
		}else{
			arrayList = ListTool.createArrayList(items);
		}
		return new ListBackedScanner<>(arrayList);
	}

	@Override
	public boolean advance(){
		++currentIndex;
		return currentIndex < items.size();
	}

	/**
	 * Exception is thrown at invalid index, like before calling advance() or calling it too many times.
	 */
	@Override
	public T getCurrent(){
		return items.get(currentIndex);
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + "[" + currentIndex + ":" + items.get(currentIndex) + "]";
	}

}
