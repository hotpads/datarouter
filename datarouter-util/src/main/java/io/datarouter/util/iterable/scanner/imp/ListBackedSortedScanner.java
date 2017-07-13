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
import java.util.Collections;

import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.iterable.scanner.sorted.BaseSortedScanner;

// Currently only used in tests.
public class ListBackedSortedScanner<T extends Comparable<? super T>>
extends BaseSortedScanner<T>{

	protected ArrayList<T> list;
	protected int currentIndex;

	public ListBackedSortedScanner(Iterable<T> ins){
		if(ins instanceof ArrayList){
			this.list = (ArrayList<T>)ins;
		}else{
			this.list = IterableTool.createArrayListFromIterable(ins);
		}
		Collections.sort(this.list);
		this.currentIndex = -1;
	}

	@Override
	public boolean advance(){
		++currentIndex;
		return currentIndex < list.size();
	}

	@Override
	public T getCurrent(){
		if(currentIndex < 0 || currentIndex >= list.size()){
			return null;
		}
		return list.get(currentIndex);
	}

	@Override
	public String toString(){
		return ListBackedSortedScanner.class.getSimpleName() + "[" + currentIndex + ":" + list.get(currentIndex) + "]";
	}

}
