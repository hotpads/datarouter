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
package io.datarouter.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.array.ArrayTool;

public class ListTool{

	public static <T> List<T> wrap(T item){
		List<T> list = new LinkedList<>();
		if(item != null){
			list.add(item);
		}
		return list;
	}

	public static <T> void replaceLast(List<T> list, T replacement){
		int lastIndex = list.size() - 1;
		list.set(lastIndex, replacement);
	}

	@SafeVarargs
	public static <T> ArrayList<T> create(T... in){
		return createArrayList(in);
	}

	@SafeVarargs
	public static <T> ArrayList<T> createArrayList(T... in){
		ArrayList<T> out = new ArrayList<>(ArrayTool.length(in));
		if(ArrayTool.isEmpty(in)){
			return out;
		}
		for(T element : in){
			out.add(element);
		}
		return out;
	}

	public static <T> List<T> nullSafe(List<T> in){
		if(in == null){
			return new LinkedList<>();
		}
		return in;
	}

	public static <T> T getLast(List<T> list){
		if(CollectionTool.nullSafeIsEmpty(list)){
			return null;
		}
		return list.get(list.size() - 1);
	}

	public static <T> Optional<T> findLast(List<T> list){
		return Optional.ofNullable(getLast(list));
	}

	@SafeVarargs
	public static <T> List<T> concatenate(Collection<T>... args){
		return Scanner.of(args).concat(Scanner::of).list();
	}

	public static <T extends Comparable<T>> int compare(List<T> as, List<T> bs){
		if(as == null){
			return bs == null ? 0 : -1;
		}
		if(bs == null){
			return 1;
		}
		Iterator<T> bi = bs.iterator();
		for(T a : as){
			if(!bi.hasNext()){
				return 1;
			} // as are longer than bs
			int comp = ComparableTool.nullFirstCompareTo(a, bi.next());
			if(comp != 0){
				return comp;
			}
		}
		return bi.hasNext() ? -1 : 0; // bs are longer than as
	}

}
